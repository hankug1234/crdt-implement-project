# crdt-implement-project
### this project reference by https://gist.github.com/Horusiath/86f60b5a247a6f5be5242950dd3733b5?ref=bartoszsypytkowski.com#file-program-fs-L7 

#### this project is operation base crdt implementation project 
#### this project goal is implement RGA structure and json document management crdt structure 


#### this project use AKKA for reliable broadcast protocal 
#### gradle for build tool 
#### spring boot for more expand my protocal to document management, chatting application 




### thank for Horusiath his blog give me a big inspire and awsome knowledge




# crdtApplication implementation
json document crdt 와 BwRga 를 활용한 간단한 공유 업무 분배 application 구현을 목표로 함 

 
 
 
# 개발 기간 
 2023/08/01 ~ 
 
# 기술 스택 
akka  

spring boot


JAVA SDK 17


GRADLE 


# 관련 기술 설명 


## CRDT 
conflict free data type 으로 동시 편집시 최종 수렴성을 유지하는 데이터 구조를 뜻함 


최종 수렴성 이란 모든 편집이 종료 된후 최종적으로 공유하는 데이터가 일관된다는 의미이며 


기존 OT(operational transformation) 알고리즘이 중앙 서버를 통해 각 node 별 편집 순서를 정렬해주어 


일관성을 유지 하던것에 반해 각 노드가 서로의 최종 일관성을 유지하는 데이터 구조를 갖음으로써 


p2p 방식의 동시 편집을 가능하게함 


### 예제 코드 


    
    static Executor sharedExcutor;
    static String replicaId1;
    static InMemoryCrdtDB<Document,List<Operation>> db1 ;
    static JsonDocument doc1;
	
	
	
	
    static String replicaId2;
    static InMemoryCrdtDB<Document,List<Operation>> db2 ;
    static JsonDocument doc2;
    
    
    
    
    Executor sharedExcutor = Executors.newFixedThreadPool(100);
    
    //node 이름 
    replicaId1 = "a";
    
    // 발생한 operation과 crdt데이터를 저장 하기 위한 메모리 기반 db -> 추후  mongoDB 적용 예정 
    db1 = new InMemoryCrdtDB<>(sharedExcutor);
    
    // json crdt duration.ofMills(50) == crdt 노드간 동기화 되는 간격 
    doc1 = new JsonDocument(replicaId1,db1,Duration.ofMillis(50));
		
    replicaId2 = "b";
    db2 = new InMemoryCrdtDB<>(sharedExcutor);
    doc2 = new JsonDocument(replicaId2,db2,Duration.ofMillis(50));

    // 두 node 연결 
    doc1.connect(doc2);
    doc2.connect(doc1);

    // {name : hankug} b node 에 할당 
    doc2.assign(Root.document().reg("name"), new Str("hankug"));
		
    JSONArray arr = new JSONArray();
    arr.put(new Str("apple"));
    arr.put(new Str("cock"));
    arr.put(new Str("candy"));
    arr.put(new Str("snack"));
    
    //{carts : ["apple","cock","candy","snack"]} b node 에 할당 
    doc2.assign(Root.document().list("carts"), arr);
		
		
    JSONObject obj = new JSONObject();
    obj.put("age", new Number(26));
    obj.put("job", new Str("Developer"));
    
    // a node에 {profile : {age : 26, job : Developer}} 할당 
    doc1.assign(Root.document().dict("profile"), obj);
    
    
    
    //BwRga object 할당후 0번째 off set 위치에 hello 삽입 
    doc1.assign(Root.document().dict("profile").reg("explain"), new ObjectTypeVal("text"));
    doc1.edit(Root.document().dict("profile").reg("explain"), Text.insert(0, "hello"));
    
    
    
    
    //동기 화를 위한 sleep
    Thread.sleep(100);
		
		
		
		
    // 동시에 5번 offset에 각각 master, hankug string 삽입 		
    doc1.edit(Root.document().dict("profile").reg("explain"), Text.insert(5, " master"));
    
    
    doc2.edit(Root.document().dict("profile").reg("explain"), Text.insert(5, " hankug"));
    
    
    
    
    //carts 배열의 1번 인덱스 값을 3번 인덱스 뒤로 위치 변경 
    doc1.move(Root.document().list("carts"), 1, 3, Location.after);
    
    // 동가화 종료
    doc1.disconnect(doc2);
    doc2.disconnect(doc1);
    
    //carts 리스트의 0 번째 인덱스 위치에 donut 삽입		
    doc1.insert(Root.document().list("carts"), 0, new Str("donut"));
    	
    doc1.connect(doc2);
    doc2.connect(doc1);
    
    // 동기화를 위한 대기 
    Thread.sleep(200);
    
    //편집 결과값 JSONObject 형식으로 조회 root 경로 아래 모든 json 값 조회
    JSONObject result2 = doc2.get(Root.document());
    
    
    JSONObject result1 = doc1.get(Root.document());
    
    
    log.info(result2.toString());
    
    
    log.info(result1.toString());
    


### state based crdt 
상태 기반 crdt 


상태 기반 crdt 란 crdt를 구현하는 방식중 하나로 각 노드가 서로의 상태를 공유 하는 방식으로 


최종 수렴성을 유지하는 방식임 


상태 기반 crdt 는 merge 연산을 통해 최종 수렴성을 유지함 merge 연산이란 각 노드가 가지고 있는 상태들을 


모두 포괄하는 상태를 만드는 연산임 




### delta base crdt 
delta 긴반 crdt란 상태 기반 crdt의 단점을 보완하기 위해 등장한 개념으로 remote 노드간 state 정보를


공유하기 위해 전체 state를 전송하는 것은 부하가 큰 연산이 됨 따라서 변경된 상태만을 전송 하는 방식으로 


동작하는 crdt가 제안 되었으며 이를 delta base crdt라 칭함 




### operation base crdt
상태 기반 crdt 는 merge 연산을 통해 최종 수렴성을 유지하기 때문에 복잡한 데이터 구조를 묘사 하는


것이 어려움 ex) tree, json 또한 remote node간 통신에 많은 비용이 소모됨 이에 편집 명령을 전송 하여 


상태를 공유하는 방식의 operation base crdt가 제안됨 




### RGA 
replicate growable array 동시 편집시 interleaving 문제 를해결 하기 위해 제안된 array 형태의 


crdt type 임 interleaving 이란 동시 편집시 상호간의 편집 의도를 보존 하지 못하는 현상임 


말 그대로 상호 편집 연산이 간섭 하여 본래의 편집 의도를 유지 하지 못하게됨  이에 array 데이터 구조 


에서 앞선 data의 위치를 기반으로 다음 데이터의 편집 위치를 결정 하므로써 array 구조에서 interleaving 


문제를 해결함 




### casual relationship

원인과 결과의 규칙적 관계 operation base crdt 에서 a,b,c 3 노드가 crdt 편집을 수행 할때 


a 의 편집 내용이 b에 반영 되었다면 

a->b 로 표현함 a->b 이후 b의 편집 내용이 c 에 반영 되었다고 할때 a 의 편집 내용이 c 에 뒤늦게 도착 


하였다면 인과 관계가 뒤바뀌게 됨 

즉 a->b->c 순서로 편집이 반영 되어야 인과 관계가 맞으나 네트워크 문제로 a->b b->c a->c 순서로 편집이 


반영 된다면 이는 인과 관계를 위반하는 것이고 최종 수렴성을 달성 하는것을 불가능 하게함 


상태 기반 crdt는 merge 연산을 통해 상태 자체를 공유 하기때문에 위와 같은 문제가 발생 하지 않음 




### reliable broadcast 
operation base crdt는 reliable broadcast 네트워크 상에서 모든 연산이 수행 되어야 하며 


같은 operation이 두번 적용되면 안됨 reliable broadcast 네트워크는 casual relationship을 보장하는 


통신 채널을 의미함 즉 a->b 관계 일때 b->c 라면 a->c 가 선행 되었어야함 



### vector clock 
각 remote node 사이의 인과 관계 순서를 확인 하기 위해 내장 시계를 사용 할 수 없음 


각 노드간 내장 시계의 시간이 일치 된다는 보장이 없으며 이를 일치 시키는 것은 아주 어려운 문제임 


따라서 상호 인과 관계가 성립 되는 것을 기준으로 편집이 일어난 순서를 정함 


a,b 의 편집이 시간상 동시에 발생 했다 더라도 b의 편집 결과가 a,c 모두에 먼저 반영 되었다면 


b의 인과 관계가 먼저 수립 된 것이기 때문에 b의 편집 결과가 인과 관계상 a를 앞서는 것으로 하며 


이러한 이관 관계를 묘사 하기 위해 lamport clock를 노드 수만큼 결합한 vector clock을 사용하여 


노드간 인과 관계를 묘사함  


# 참고 opensource
bartoszsypytkowski 님의 F# 기반 op base crdt 구현 코드 


https://gist.github.com/Horusiath/86f60b5a247a6f5be5242950dd3733b5?ref=bartoszsypytkowski.com




Go / typescript 기반 국내 crdt 구현 오픈소스 커뮤니티  


https://yorkie.dev/




데이터 중심 애플리케이션 설계의 저자이신 martin kleppmann 님의


 A Conflict-Free Replicated JSON Datatype 논문을 scala 기반으로 구현한 repository


https://github.com/fthomas/crjdt/tree/master/modules/core/src/main/scala/eu/timepit/crjdt/core




crdt 관련 논문을 모아놓은 repository


https://github.com/alangibson/awesome-crdt


# 참고 논문 
A Conflict-Free Replicated JSON Datatype 


json document을 crdt 구조로 설계 하기 위한 방법을 기술한 논문 


동시 json 편집시 각 데이터를 tagtype을 활용하여 보존하는 방식으로 동시성 문제를 해결 하였음 


기술된 편집 방식을 따를 경우 최종 수렴이 보장됨을 증명함 

link : https://arxiv.org/abs/1608.03960




Replicated abstract data types: Building blocks for collaborative applications


RGA,RHT,RFA CRDT 구조를 소개하고 RGA CRDT구조를 제안 하는 논문


link : http://csl.skku.edu/papers/jpdc11.pdf
# 참고 블로그 
https://hackerwins.github.io/2018-10-26/a-conflict-free-replicated-json-datatype


https://www.bartoszsypytkowski.com/operation-based-crdts-registers-and-sets/

 
 

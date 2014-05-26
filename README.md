TO- Do(140526)
=============

### 오늘 해본 것 & 짧게 공대 신양에서 이야기해 본 것

- topterms 을 살펴보고 여기서 `march`같은 키워드는 stopword로 추가시킨다(파일에 수동으로 추가하면 안됨으로 코드로 구현)
- topterm에서 top10을 stopwords에 추가해서 expansion 하기

범준 시도 해본 것 / 영일 준희형 읽어주세요(140526)
--------------------------

- [queryModifier]단어가 3개인 경우 `#combine(#od:1(termA termB termC))` 같은 방식으로 A, B, C 단어가 '붙어있는 상태로 순서가 유지되도록' 검색되게 바꿔봄
  - `balance of payments`, `earning and earning` 같은 경우엔 tokenizer 가 of/and 을 빼지 않음. 이런 디테일한 것들 다 손봐야함.
  - 그러면 `balance of payments` 쿼리에서 1000개 검색되던게 이제는 50개 검색됨
  - (BUT) 논리적으로는 점수가 좋아져야할거 같은데.. `#combine`이 올바른 galago 질의어가 아닌지 결과가 더 안 좋아짐
  - 어쨋든 query term 3개이상인 경우에 대해선 **뭔가 조치**가 필요하겠다는 사실을 발견함!!

- [Intappscorer]generic 머셔를 그냥 base 형태로 구현하면 오히려 안 좋아짐(해봄)
  - 약간의 수정(posWeight을 추가함)을 해서 기존 IntappScorer 보다 좋아짐

**..............여러 시도를 해보고 난 뒤의 결론..........**

> 앞으로 할 일은 69개 쿼리에 대해서 'corpus'에 실제로 다 검색을 해보고 그 검색된 결과를 본 뒤 코드에선 주어진 쿼리를 어떻게 tokenize 해서 실제로 검색했는지 눈으로!! 비교해봄.(ㅠㅠ)

- '실제 검색된 문서 갯수' 까지 따져가면서 살펴봐야함(e.g. 100번쯤 밖에 안나온 단어인데 문서가 1000개 검색됬다면 이상한 것임)
- 아래는 시도해볼 수 있는 것들을 정리해본 것(아직 다 시도 안해봄, 무궁무진하게 응용해서 overfitttttttttttttting 할 수 있을 듯 `-_-;;`)
  - [overfitting- 1] U.S. dollar 같은 것도 토큰화해서 검색에 넣지 말고 'U.S. dollar' 통째로 넣어야 더 좋은 결과가 나올 것 같음
  - [overfitting- 2] 69 개의 쿼리 중 3단어짜리 쿼리들을 보면 'balance of payments' 같이 의미상으로 '꼭 붙어서 나오면 좋을거 같은(!) 경우'가 존재. 이런것에 대해서 token화 되지 않게 수정
  - [overfitting- 3] 토큰화이저가 'of/and' 같은 단어를 stopword 로 빼주질 않음. 우리가 코딩해서 빼면 더 좋아질 것 같음
  - [overfitting- 4] 두 단어 이상의 쿼리들을 보면 of 나 and 가 없으면 '각각 하나씩 나온 것'에는 1.0 가중치 / '두 단어가 동시에 나온 문서' 에 대해선 0.5 씩 가중치 하는 방식으로 수정
    - `#combine:0=0.85:1=0.1:2=0.05( #combine(term term) #combine(#od:1(term term) #combine(#uw:8(term term ))))`
  - 기타 등등.. 추가 제보 바람;

internet-application-2014
=========================

Repository for internet-application-2014 (SNU IE)

* 원래 프로젝트에서 src 디렉토리만 커밋하도록 변경한 저장소
* 각자 수정한 내용이 서로에게 영향 미치는 것을 막기 위해 다음과 경로 밑에 각자 이름을 딴 패키지를 만듦
* eg.) src\course\java\scorer\termproject\youngilcho\
* 이런 상태에서 각자 코드를 실행 시키려면 아래와 같이 두개 파일의 수정이 필요함
* 이 방법을 쓰면 자신의 것은 물론 다른 사람의 QueryModifier와 IntappScorer도 즉각적으로 테스트 가능
- src\main\java\org\galagosearch\core\tools\BatchSearch.java
```java
// BSD License (http://www.galagosearch.org/license)
package org.galagosearch.core.tools;

import java.io.PrintStream;
import java.util.List;
import org.galagosearch.core.retrieval.Retrieval;
import org.galagosearch.core.retrieval.ScoredDocument;
import org.galagosearch.core.retrieval.query.Node;
import org.galagosearch.core.retrieval.query.SimpleQuery;
import org.galagosearch.core.retrieval.query.StructuredQuery;
import org.galagosearch.tupleflow.Parameters;

import scorer.termproject.youngilcho.QueryModifier; // 이 부분 수정해야 함

/**
 *
 * @author trevor
 */
public class BatchSearch {
```
- src\main\java\org\galagosearch\core\tools\BatchSearch.java
```java
// BSD License (http://www.galagosearch.org/license)

package org.galagosearch.core.tools;

import org.galagosearch.core.retrieval.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.galagosearch.core.parse.Document;
import org.galagosearch.core.retrieval.query.Node;
import org.galagosearch.core.retrieval.query.SimpleQuery;
import org.galagosearch.core.retrieval.query.StructuredQuery;
import org.galagosearch.core.store.DocumentStore;
import org.galagosearch.core.store.SnippetGenerator;
import org.galagosearch.tupleflow.Parameters;

import scorer.termproject.youngilcho.QueryModifier; // 이 부분 수정해야 함

/**
 *
 * @author trevor
 */
```



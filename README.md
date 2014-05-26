TO- Do(140526)
=============

오늘 해본 것

- topterms 을 살펴보고 여기서 `march`같은 키워드는 stopword로 추가시킨다(파일에 수동으로 추가하면 안됨으로 코드로 구현)

todo

- topterm에서 top10을 stopwords에 추가해서 expansion 하기

범준 시도 해본 것 (140526)
--------------------------

- 단어가 3개인 경우 `#combine(#od:1(termA termB))` 같은 방식으로 A, B 단어가 '붙어있는 상태로 순서가 유지되도록' 바꿈
  - 그러면 balance of payments 쿼리에서 1000개 검색되던게 이제는 50개 검색됨
- generic 머셔를 그냥 base 형태로 구현하면 오히려 안 좋아짐(해봄)
  - 약간의 수정을 해야 현재 IntappScorer 보다 좋아질 듯
- 앞으로 더 해볼 것: query 69개 읽고 수정하기

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



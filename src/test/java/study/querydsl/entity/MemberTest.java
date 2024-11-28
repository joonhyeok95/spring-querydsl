package study.querydsl.entity;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;

@SpringBootTest
@Transactional
public class MemberTest {
    @Autowired
    EntityManager em;

    @Test
    public void testEntity() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
        
        // 초기화
        em.flush();
        em.clear();
        
        //확인
        List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();
        
        for (Member member : members) {
            System.out.println("member = " + member);
            System.out.println("-> member.team = " + member.getTeam());
        }
    }
    
    @BeforeEach

    @Test
    public void before() {
        queryFactory = new JPAQueryFactory(em);
        
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);
        
        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }
    
    @Test
    public void startJPQL() { // 런타임 중에 오류가 발생할 수 있어 JPQL의 아쉬운점
        // member1을 찾아라.
        String sql = "select m from Member m where m.username = :username";
        
        Member findMember = em.createQuery(sql, Member.class)
                            .setParameter("username", "member1")
                            .getSingleResult();
        assertThat(findMember.getUsername()).isEqualTo("member1");
        
    }
    JPAQueryFactory queryFactory;
    @Test
    public void startQuerydsl() { // 자바컴파일시점에 오류를 잡을 수 있는 강점.
//        queryFactory = new JPAQueryFactory(em); // before 에 넣어도 동시성 문제가 없다.
        //QMember 없으면 Eclipse 기준 [gradle tasks] > [project 선택] > [build폴더] > [build]
//        QMember m = new QMember("m");
        
        Member findMember = queryFactory
                .select(QMember.member)
                .from(QMember.member)
                .where(QMember.member.username.eq("member1"))
                .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }
    
    @Test
    public void search() {
        Member findMember = queryFactory
        .selectFrom(QMember.member)
        .where(QMember.member.username.eq("member1")
                .and(QMember.member.age.eq(10)))
        .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }
    
    @Test
    public void searchAndParam() {
        Member findMember = queryFactory
        .selectFrom(QMember.member)
        .where(
                QMember.member.username.eq("member1"), // , 이 and와 같다
                QMember.member.age.eq(10)
        )
        .fetchOne();

        assertThat(findMember.getUsername()).isEqualTo("member1");
    }
    
    @Test
    public void resultFetch() {
        // 리스트
        List<Member> fetch = queryFactory
                .selectFrom(QMember.member)
                .fetch();
        // 단건 - 두개이상이 조회되면 NonUniqueResultException
        Member fetchOne = queryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.username.eq("member1"))
                .fetchOne();
        // 단건 - 첫번째
        Member fetchFirst = queryFactory
                .selectFrom(QMember.member)
                .fetchFirst();
        
        // 쿼리 두방 나감
        QueryResults<Member> results = queryFactory
                .selectFrom(QMember.member)
                .fetchResults(); // deprecated
        results.getTotal();
        List<Member> content = results.getResults();
        
        long total = queryFactory
                .selectFrom(QMember.member)
                .fetchCount(); // deprecated
    }
    /*
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 오름차순(asc)
     * 단 2에서 회원이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));
        
        em.flush();
        em.clear();
        
        List<Member> result = queryFactory
                .selectFrom(QMember.member)
                .where(QMember.member.age.eq(100))
                .orderBy(QMember.member.age.desc(), QMember.member.username.asc().nullsLast())
                .fetch();
            
        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();
        
        
    }
    
    @Test
    public void paging1() {
        QueryResults<Member> queryResult = queryFactory
                .selectFrom(QMember.member)
                .orderBy(QMember.member.age.desc())
                .offset(1) // paging
                .limit(2) // paging
                .fetchResults();
        // 페이징쿼리가 복잡하면 카운팅 쿼리를 별도로 분리해야함
        
        assertThat(queryResult.getTotal()).isEqualTo(4);
        assertThat(queryResult.getLimit()).isEqualTo(2);
        assertThat(queryResult.getOffset()).isEqualTo(1);
        assertThat(queryResult.getResults().size()).isEqualTo(2);

    }
    
    @Test
    public void aggregation() {
        // querydsl 패키지의 Tuple
        List<Tuple> result = queryFactory
            .select(
                    QMember.member.count(),
                    QMember.member.age.sum(),
                    QMember.member.age.avg(),
                    QMember.member.age.max(),
                    QMember.member.age.min()
            )
            .from(QMember.member)
            .fetch();
        Tuple tuple = result.get(0);
        assertThat(tuple.get(QMember.member.count())).isEqualTo(4);
        assertThat(tuple.get(QMember.member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(QMember.member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(QMember.member.age.max())).isEqualTo(40);
        assertThat(tuple.get(QMember.member.age.min())).isEqualTo(10);
        
    }
    
    @Test
    public void group() throws Exception {
        List<Tuple> result = queryFactory
                    .select(QTeam.team.name, QMember.member.age.avg())
                    .from(QMember.member)
                    .join(QMember.member.team, QTeam.team)
                    .groupBy(QTeam.team.name)
                    .fetch();
        
        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);
        
        assertThat(teamA.get(QTeam.team.name)).isEqualTo("teamA");
        assertThat(teamA.get(QMember.member.age.avg())).isEqualTo(15); // 10 + 20 / 2
        
        assertThat(teamB.get(QTeam.team.name)).isEqualTo("teamB");
        assertThat(teamB.get(QMember.member.age.avg())).isEqualTo(35); // 30 + 40 / 2
    }
    
    @Test // 팀 A에 소속된 모든 회원
    public void NAME() throws Exception {
        List<Member> result = queryFactory
            .selectFrom(QMember.member)
            .leftJoin(QMember.member.team, QTeam.team) // .join == .leftJoin
            .where(QTeam.team.name.eq("teamA"))
            .fetch();
        
        assertThat(result).extracting("username").containsExactly("member1","member2");
    }
    // 세타조인 : 연관관계가 없어도 조인할 수 있는 경우
    // ex: 회원의 이름이 팀 이름과 같은 회원 조회
    @Test
    public void theta_join() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));
        
        List<Member> result = queryFactory
            .select(QMember.member)
            .from(QMember.member, QTeam.team)
            .where(QMember.member.username.eq(QTeam.team.name))
            .fetch();
        
        assertThat(result)
            .extracting("username")
            .containsExactly("teamA", "teamB");
    }
 // 세타조인 : 연관관계가 없어도 조인할 수 있는 경우
    // ex: 회원의 이름이 팀 이름과 같은 대상 외부 조회
    @Test
    public void theta_join_no_relation() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));
        
        List<Tuple> result = queryFactory
            .select(QMember.member, QTeam.team)
            .from(QMember.member)
            .leftJoin(QTeam.team).on(QMember.member.username.eq(QTeam.team.name))
            .fetch();
        
        for (Tuple tuple : result) {
            System.out.println("세타..tuple = " + tuple);
        }
        
//        assertThat(result)
//            .extracting("username")
//            .containsExactly("teamA", "teamB");
    }
    /*
     * ex) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
     * JPQL: select m, t from Member m left join m.team t on t.name = 'teamA'
     */
    @Test
    public void join_on_filtering() {
        List<Tuple> result = queryFactory
                .select(QMember.member, QTeam.team)
                .from(QMember.member)
                .leftJoin(QMember.member.team, QTeam.team)
                    .on(QTeam.team.name.eq("teamA"))
                    .fetch();
        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }
    
    @PersistenceUnit
    EntityManagerFactory emf;
    
    @Test
    public void fetchJoinNo() {
        em.flush();
        em.clear();
        
        Member findMember = queryFactory
            .selectFrom(QMember.member)
            .where(QMember.member.username.eq("member1"))
            .fetchOne();
        
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("패치조인 미적용").isFalse();
    }    
    @Test
    public void fetchJoinUse() {
        em.flush();
        em.clear();
        
        Member findMember = queryFactory
            .selectFrom(QMember.member)
            .join(QMember.member.team, QTeam.team).fetchJoin()
            .where(QMember.member.username.eq("member1"))
            .fetchOne();
        
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("패치조인 적용").isTrue();
    }
    
    /*
     * 나이가 가장 많은 회원 조회
     */
    @Test
    public void subQuery() {
        QMember memberSub = new QMember("memberSub");
        
        List<Member> result = queryFactory
            .selectFrom(QMember.member)
            .where(QMember.member.age.eq(
                    JPAExpressions
                        .select(memberSub.age.max())
                        .from(memberSub)
                    ))
            .fetch();
        
        assertThat(result).extracting("age")
            .containsExactly(40);
    }

    /*
     * 나이가 평균 이상인 회원 조회
     */
    @Test
    public void subQueryGoe() {
        QMember memberSub = new QMember("memberSub");
        
        List<Member> result = queryFactory
            .selectFrom(QMember.member)
            .where(QMember.member.age.goe(
                    JPAExpressions
                        .select(memberSub.age.avg())
                        .from(memberSub)
                    ))
            .fetch();
        
        assertThat(result).extracting("age")
            .containsExactly(30, 40);
    }
    /*
     * 나이가 10살을 넘은 회원 조회 ( where 절에 써보기 ) 
     */
    @Test
    public void subQueryIn() {
        QMember memberSub = new QMember("memberSub");
        
        List<Member> result = queryFactory
            .selectFrom(QMember.member)
            .where(QMember.member.age.in(
                    JPAExpressions
                        .select(memberSub.age)
                        .from(memberSub)
                        .where(memberSub.age.gt(10)) // 나이가 10보다 큰사람
                    ))
            .fetch();
        
        assertThat(result).extracting("age")
            .containsExactly(20, 30, 40);
    }
    /*
     * 나이가 10살을 넘은 회원 조회 ( 필드에 써보기 ) 
     */
    @Test
    public void selectSubQuery() {
        QMember memberSub = new QMember("memberSub");
        
        List<Tuple> result = queryFactory
            .select(QMember.member.username,
                    JPAExpressions
                        .select(memberSub.age.avg())
                        .from(memberSub)
                )
            .from(QMember.member)
            .fetch();
        
        for (Tuple tuple : result) {
            System.out.println("sub query - " + tuple);
        }
    }
    
    @Test
    public void basicCase() {
        List<String> result = queryFactory
            .select(
                    QMember.member.age
                    .when(10).then("열살")
                    .when(20).then("스무살")
                    .otherwise("기타")
            )
            .from(QMember.member)
            .fetch();
        for (String s : result) {
            System.out.println("s="+s);
        }
    }
    
    @Test
    public void complexCase() {

        List<String> result = queryFactory
            .select(
                    new CaseBuilder()
                    .when(QMember.member.age.between(0, 20)).then("0~20살")
                    .when(QMember.member.age.between(21, 30)).then("21~30살")
                    .otherwise("기타")
            )
            .from(QMember.member)
            .fetch();
        for (String s : result) {
            System.out.println("s="+s);
        }
    }
    
    @Test
    public void constant() {
        List<Tuple> result = queryFactory
            .select(QMember.member.username, Expressions.constant("A"))
            .from(QMember.member)
            .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple="+tuple);
        }
    }
    
    @Test
    public void concat() {
        //{username}_{age}
        List<String> result = queryFactory
            .select(QMember.member.username.concat("_").concat(QMember.member.age.stringValue()))
            .from(QMember.member)
            .where(QMember.member.username.eq("member1"))
            .fetch();
        for (String s : result) {
            System.out.println("s="+s);
        }
    }
        
}

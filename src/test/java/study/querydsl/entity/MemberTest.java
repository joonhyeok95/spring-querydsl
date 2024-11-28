package study.querydsl.entity;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

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
}

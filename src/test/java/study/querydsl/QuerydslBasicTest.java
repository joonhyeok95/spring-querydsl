package study.querydsl;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {
    
    @Autowired
    EntityManager em;
    
    JPAQueryFactory queryFactory;
    
    // querydsl 중급 문법
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
    public void simpleProjection() {
        List<String> result = queryFactory
                .select(QMember.member.username)
                .from(QMember.member)
                .fetch();
        
        for (String string : result) {
            System.out.println("simpleProjection = " + string);
        }
    }

    @Test
    public void tupleProjection() {
        List<Tuple> result = queryFactory
                .select(QMember.member.username, QMember.member.age)
                .from(QMember.member)
                .fetch();
        
        for (Tuple tuple : result) {
            String username = tuple.get(QMember.member.username);
            Integer age = tuple.get(QMember.member.age);
            System.out.println("tupleProjection username = " + username);
            System.out.println("tupleProjection age = " + age);
        }
    }
    
    /////// 프로젝션 dto /////////////
    @Test
    public void findDtoByJPQL() {
        List<MemberDto> result = em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
                .getResultList();
        for (MemberDto memberDto : result) {
            System.out.println(memberDto);
        }
    }
    @Test
    public void findDtoBySetter() {
        List<MemberDto> result = queryFactory
            .select(Projections.bean(MemberDto.class, // .bean 비어있는 기본생성자가 필수
                    QMember.member.username, 
                    QMember.member.age)) 
            .from(QMember.member)
            .fetch();
        for (MemberDto memberDto : result) {
            System.out.println(memberDto);
        }
    }
    @Test
    public void findDtoByField() {
        List<MemberDto> result = queryFactory
            .select(Projections.fields(MemberDto.class,  // .fields
                    QMember.member.username, 
                    QMember.member.age))
            .from(QMember.member)
            .fetch();
        for (MemberDto memberDto : result) {
            System.out.println(memberDto);
        }
    }
    @Test
    public void findDtoByConstructor() {
        List<MemberDto> result = queryFactory
            .select(Projections.constructor(MemberDto.class,  // .constructor
                    QMember.member.username, 
                    QMember.member.age))
            .from(QMember.member)
            .fetch();
        for (MemberDto memberDto : result) {
            System.out.println(memberDto);
        }
    }
    @Test // UserDto 로 하기
    public void findUserDto() {
        QMember memberSub = new QMember("memberSub");
        List<UserDto> result = queryFactory
            .select(Projections.fields(UserDto.class,  // .fields
                    QMember.member.username.as("name"), // UserDto의 필드로 맞춰줘야함 
                    // 서브쿼리로 필드를 하는 방법
                    ExpressionUtils.as(JPAExpressions
                            .select(memberSub.age.max())
                                .from(memberSub), "age")))
            .from(QMember.member)
            .fetch();
        for (UserDto memberDto : result) {
            System.out.println(memberDto);
        }
    }
    @Test // UserDto 로 하기
    public void findUserDtoByConstructor() {
        List<UserDto> result = queryFactory
            .select(Projections.constructor(UserDto.class,  // .fields
                    QMember.member.username,
                    QMember.member.age))
            .from(QMember.member)
            .fetch();
        for (UserDto memberDto : result) {
            System.out.println(memberDto);
        }
    }
    
    // QueryProjection
//    @Test
//    public void findDtoByQueryProjection() {
//        List<MemberDto> result = queryFactory
//        .select(new QMemberDto(QMember.member.username, QMember.member.age))
//        .from(QMember.member)
//        .fetch();
//    }
}

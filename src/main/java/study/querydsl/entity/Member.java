package study.querydsl.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "username", "age"}) // 본인이 소유한 필드만 해야 무한루프안걸림
public class Member {

    @Id @GeneratedValue
    private Long id;
    private String username;
    private int age;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if(team != null) {
            changeTeam(team);
        }
    }

    public Member(String username, int age) {
        this(username, 0, null);
//        this.username = username;
//        this.age = age;
    }

    public Member(String username) {
        this(username, 0);
//        this.username = username;
//        this.age = 0;
    }

    private void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
    
}

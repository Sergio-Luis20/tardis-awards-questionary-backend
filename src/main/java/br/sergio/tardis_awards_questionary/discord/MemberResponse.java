package br.sergio.tardis_awards_questionary.discord;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse implements Comparable<MemberResponse> {

    private String discordId, name, avatarUrl, note;
    private MemberResponse second;

    public MemberResponse(UserSnowflake sf) {
        switch (sf) {
            case Member member -> {
                this.discordId = member.getId();
                this.name = member.getEffectiveName();
                this.avatarUrl = member.getEffectiveAvatarUrl();
            }
            case User user -> {
                this.discordId = user.getId();
                this.name = user.getEffectiveName();
                this.avatarUrl = user.getEffectiveAvatarUrl();
            }
            default -> {
                this.discordId = sf.getId();
                this.name = sf.getAsMention();
                this.avatarUrl = sf.getDefaultAvatarUrl();
            }
        }
    }

    public MemberResponse(UserSnowflake first, UserSnowflake second) {
        this(first);
        this.second = new MemberResponse(second);
    }

    @Override
    public int compareTo(@NotNull MemberResponse o) {
        return discordId.compareTo(o.discordId);
    }

}

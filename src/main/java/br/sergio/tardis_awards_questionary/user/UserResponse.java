package br.sergio.tardis_awards_questionary.user;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.UserSnowflake;

public record UserResponse(String discordId, String name, String avatarUrl, boolean voted, boolean admin) {

    public UserResponse(UserSnowflake sf, boolean voted, boolean admin) {
        String discordId, name, avatarUrl;

        switch (sf) {
            case Member member -> {
                discordId = member.getId();
                name = member.getEffectiveName();
                avatarUrl = member.getEffectiveAvatarUrl();
            }
            case User user -> {
                discordId = user.getId();
                name = user.getEffectiveName();
                avatarUrl = user.getEffectiveAvatarUrl();
            }
            default -> {
                discordId = sf.getId();
                name = sf.getAsMention();
                avatarUrl = sf.getDefaultAvatarUrl();
            }
        }

        this(discordId, name, avatarUrl, voted, admin);
    }

}
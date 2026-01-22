package br.sergio.tardis_awards_questionary.user;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public record BasicUserResponse(String discordId, String name, String avatarUrl) {

    public BasicUserResponse(Member member) {
        this(member.getId(), member.getEffectiveName(), member.getEffectiveAvatarUrl());
    }

    public BasicUserResponse(User user) {
        this(user.getId(), user.getEffectiveName(), user.getEffectiveAvatarUrl());
    }

}

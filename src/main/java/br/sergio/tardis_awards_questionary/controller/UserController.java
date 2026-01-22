package br.sergio.tardis_awards_questionary.controller;

import br.sergio.tardis_awards_questionary.discord.DiscordService;
import br.sergio.tardis_awards_questionary.discord.MemberResponse;
import br.sergio.tardis_awards_questionary.user.AppUser;
import br.sergio.tardis_awards_questionary.user.UserResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
public class UserController {

    private DiscordService discordService;

    @GetMapping("/discord-members")
    public ResponseEntity<List<MemberResponse>> members() {
        return ResponseEntity.ok(discordService.getMembers().stream().map(MemberResponse::new).toList());
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal AppUser user) {
        return ResponseEntity.ok(new UserResponse(discordService.getUserSnowflake(user.getDiscordId()), user.hasVoted(), user.isAdmin()));
    }

}

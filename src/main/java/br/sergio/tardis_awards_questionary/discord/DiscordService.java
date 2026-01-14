package br.sergio.tardis_awards_questionary.discord;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
@Service
@Slf4j
public class DiscordService implements AutoCloseable {

    private JDA jda;
    private Guild tardis;
    private String authenticatedUri;

    public DiscordService(@Value("${discord.bot-token}") String token,
                          @Value("${discord.tardis-id}") long tardisId,
                          @Value("${discord.authenticated-uri}") String authenticatedUri) throws InterruptedException {
        this.authenticatedUri = Objects.requireNonNull(authenticatedUri, "authenticatedUri");

        log.info("Creating JDA");
        jda = JDABuilder.create(token, Arrays.asList(GatewayIntent.values()))
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .build();

        log.info("Awaiting JDA ready");
        jda.awaitReady();
        log.info("JDA ready! Client id: {}", jda.getSelfUser().getId());

        tardis = Objects.requireNonNull(jda.getGuildById(tardisId), "Tardis is null");
    }

    public List<Member> getMembers() {
        return tardis.getMembers().stream().filter(member -> !member.getUser().isBot()).toList();
    }

    public boolean containsMemberById(String memberId) {
        for (Member member : getMembers()) {
            if (memberId.equals(member.getId())) {
                return true;
            }
        }
        return false;
    }

    public Member getMember(String id) {
        return tardis.getMemberById(id);
    }

    @Override
    public void close() throws Exception {
        jda.shutdown();
        jda.awaitShutdown();
    }

}

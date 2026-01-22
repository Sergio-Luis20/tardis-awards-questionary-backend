package br.sergio.tardis_awards_questionary.controller;

import br.sergio.tardis_awards_questionary.discord.DiscordService;
import br.sergio.tardis_awards_questionary.discord.MemberResponse;
import br.sergio.tardis_awards_questionary.questionary.Answer;
import br.sergio.tardis_awards_questionary.questionary.Question;
import br.sergio.tardis_awards_questionary.questionary.QuestionAnswer;
import br.sergio.tardis_awards_questionary.questionary.QuestionaryService;
import br.sergio.tardis_awards_questionary.user.AppUser;
import br.sergio.tardis_awards_questionary.user.BasicUserResponse;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/question")
@AllArgsConstructor
public class QuestionaryController {

    private QuestionaryService service;
    private DiscordService discordService;

    @GetMapping("/questions")
    public ResponseEntity<List<Question>> questions() {
        return ResponseEntity.ok(Question.questions());
    }

    @Transactional
    @PostMapping("/answer")
    public ResponseEntity<Answer> answer(@RequestBody Answer answer) {
        if (service.exists(answer.getDiscordId())) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        if (!discordService.containsMemberById(answer.getDiscordId())) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Answer created = service.post(answer);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("")
                .pathSegment("question", "my-answer")
                .build()
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/my-answer")
    public ResponseEntity<Answer> myAnswer(@AuthenticationPrincipal AppUser user) {
        return service.getAnswer(String.valueOf(user.getDiscordId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all-answers")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<UserAnswerResponse>> allAnswers() {
        List<UserAnswerResponse> response = service.getAll().stream()
                .map(answer -> new UserAnswerResponse(answer, this::toUser, this::toMemberResponse))
                .toList();
        return ResponseEntity.ok(response);
    }

    private BasicUserResponse toUser(String discordId) {
        return discordService.getMember(discordId)
                .map(BasicUserResponse::new)
                .orElseGet(() -> new BasicUserResponse(discordService.getUser(discordId)));
    }

    private MemberResponse toMemberResponse(String discordId) {
        if (discordId.indexOf('_') < 0) {
            return new MemberResponse(discordService.getUserSnowflake(discordId));
        } else {
            String[] ids = discordId.split("_");
            UserSnowflake first = discordService.getUserSnowflake(ids[0]);
            UserSnowflake second = discordService.getUserSnowflake(ids[1]);
            return new MemberResponse(first, second);
        }
    }

    public record UserAnswerResponse(BasicUserResponse member, Set<QuestionAnswerResponse> answers) {

        public UserAnswerResponse(Answer answer, Function<String, BasicUserResponse> converter, Function<String, MemberResponse> memberConverter) {
            this(converter.apply(answer.getDiscordId()), answer.getAnswers().stream()
                    .map(questionAnswer -> new QuestionAnswerResponse(questionAnswer, memberConverter))
                    .collect(Collectors.toSet()));
        }

    }

    public record QuestionAnswerResponse(int questionId, List<MemberResponse> members) {

        public QuestionAnswerResponse(QuestionAnswer answer, Function<String, MemberResponse> converter) {
            this(answer.getQuestionId(), answer.getMembersIds().stream().map(converter).toList());
        }

    }

}

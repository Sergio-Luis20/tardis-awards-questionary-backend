package br.sergio.tardis_awards_questionary.questionary;

import br.sergio.tardis_awards_questionary.discord.DiscordService;
import br.sergio.tardis_awards_questionary.discord.MemberResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.UserSnowflake;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

@Component
@AllArgsConstructor
@Slf4j
public class QuestionConfigurer implements CommandLineRunner {

    private DiscordService discordService;

    @Override
    public void run(String... args) throws Exception {
        InputStream stream = getClass().getResourceAsStream("/questions.csv");
        if (stream == null) {
            throw new NullPointerException("File \"questions.csv\" not found in classpath");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
            List<Question> questions = new ArrayList<>();
            Map<String, String> notes = new HashMap<>();
            for (String line; (line = reader.readLine()) != null;) {
                if (line.startsWith("*")) {
                    // Free-choice question
                    String[] tokens = line.substring(1).split(",");
                    int questionId = Integer.parseInt(tokens[0]);
                    String questionText = tokens[1];
                    String description = tokens[2].replace('§', ',');
                    int numAnswers = Integer.parseInt(tokens[3]);
                    questions.add(new Question(questionId, questionText, description, numAnswers, null));
                    continue;
                }
                notes.clear();
                String[] tokens = line.split(",");
                int questionId = Integer.parseInt(tokens[0]);
                String questionText = tokens[1];
                String description = tokens[2].replace('§', ',');
                String[] idsTokens = Arrays.copyOfRange(tokens, 3, tokens.length);
                String[] discordIds = new String[idsTokens.length];
                for (int i = 0; i < discordIds.length; i++) {
                    String token = idsTokens[i];
                    int spaceIndex = token.indexOf(' ');
                    if (spaceIndex < 0) {
                        discordIds[i] = token;
                        continue;
                    }
                    String id = token.substring(0, spaceIndex);
                    discordIds[i] = id;
                    String note = token.substring(spaceIndex + 1);
                    if (note.startsWith("(") && note.endsWith(")")) {
                        note = note.substring(1, note.length() - 1);
                    }
                    notes.put(id, note);
                }
                Question question = simpleQuestion(questionId, questionText, description, discordIds);
                Set<MemberResponse> members = question.options();
                for (MemberResponse member : members) {
                    String discordId = member.getDiscordId();
                    if (notes.containsKey(discordId)) {
                        member.setNote(notes.get(discordId));
                    }
                }
                questions.add(question);
            }
            Question.setQuestions(questions);
        }
    }

    private Question simpleQuestion(int id, String question, String description, String[] discordIds) {
        Set<MemberResponse> members = new HashSet<>(discordIds.length);
        for (String discordId : discordIds) {
            if (discordId.indexOf('_') >= 0) {
                String[] ids = discordId.split("_");
                UserSnowflake first = discordService.getUserSnowflake(ids[0]);
                UserSnowflake second = discordService.getUserSnowflake(ids[1]);
                members.add(new MemberResponse(first, second));
            } else {
                members.add(new MemberResponse(discordService.getUserSnowflake(discordId)));
            }
        }
        return new Question(id, question, description, 1, members);
    }

}

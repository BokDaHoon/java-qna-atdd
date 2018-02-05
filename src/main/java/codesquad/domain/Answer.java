package codesquad.domain;

import javax.persistence.*;
import javax.validation.constraints.Size;

import codesquad.dto.AnswerDto;
import codesquad.etc.CannotDeleteException;
import codesquad.etc.UnAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import support.domain.AbstractEntity;
import support.domain.UrlGeneratable;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
public class Answer extends AbstractEntity implements UrlGeneratable {

    private static final Logger log = LoggerFactory.getLogger(Answer.class);

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_answer_writer"))
    private User writer;

    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_answer_to_question"))
    private Question question;

    @Size(min = 5)
    @Lob
    private String contents;

    private boolean deleted = false;

    public Answer() {
    }

    public Answer(User writer, String contents) {
        this.writer = writer;
        this.contents = contents;
    }

    public Answer(Long id, User writer, Question question, String contents) {
        super(id);
        this.writer = writer;
        this.question = question;
        this.contents = contents;
        this.deleted = false;
    }

    public Answer setWriter(User writer) {
        this.writer = writer;
        return this;
    }

    public Answer setQuestion(Question question) {
        this.question = question;
        return this;
    }

    public Answer setContents(String contents) {
        this.contents = contents;
        return this;
    }

    public Answer setDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public User getWriter() {
        return writer;
    }

    public Question getQuestion() {
        return question;
    }

    public String getContents() {
        return contents;
    }

    public void toQuestion(Question question) {
        this.question = question;
    }

    public boolean isOwner(User loginUser) {
        return writer.equals(loginUser);
    }

    public boolean isDeleted() {
        return deleted;
    }

    @Override
    public String generateUrl() {
        return String.format("%s/answers/%d", question.generateUrl(), getId());
    }

    @Override
    public String toString() {
        return "Answer{" +
                "id=" + getId() +
                ", writer=" + writer +
                ", question=" + question +
                ", contents='" + contents + '\'' +
                ", deleted=" + deleted +
                '}';
    }

    public DeleteHistory delete(User loginUser) throws CannotDeleteException {
        if (this.writer.equals(loginUser)){
            this.deleted = true;
            return new DeleteHistory(ContentType.ANSWER,
                    getId(),
                    loginUser,
                    LocalDateTime.now());
        }

        throw new CannotDeleteException("The user has no authorization.");
    }

    public AnswerDto toAnswerDto() {
        return new AnswerDto()
                .setId(getId())
                .setWriter(this.writer)
                .setContents(this.contents)
                .setQuestion(this.question);
    }

    public void update(User loginUser, Answer newAnswer) {
        if (writer.equals(loginUser)) {
            this.contents = newAnswer.getContents();
            return;
        }
        throw new UnAuthorizedException();
    }
}

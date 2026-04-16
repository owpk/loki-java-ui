package owpk.jloki.core.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogQueryFilter {
    private String field; // например "message"
    private String operator; // =, !=, =~, !~
    private String value; // regex или строка

}

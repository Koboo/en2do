package eu.koboo.en2do.example;

import eu.koboo.en2do.annotation.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class POJO {

    @Id
    private String string;
    private Double number;
}
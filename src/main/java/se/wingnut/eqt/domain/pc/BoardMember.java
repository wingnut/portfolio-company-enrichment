package se.wingnut.eqt.domain.pc;

import java.io.Serializable;

public record BoardMember(String title, String name, Person person) implements Serializable {
    
}


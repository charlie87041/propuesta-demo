package com.cookiesstore.pos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.sql.Types;
import org.hibernate.annotations.JdbcTypeCode;

@Entity
@Table(name = "currencies")
public class PosCurrency {

    @Id
    @JdbcTypeCode(Types.CHAR)
    @Column(name = "code", length = 3, nullable = false, columnDefinition = "char(3)")
    private String code;

    public String getCode() {
        return code;
    }
}

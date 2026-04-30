package com.cookiesstore.pos.domain;

import com.cookiesstore.common.entities.Source;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "admin_source_pos_users")
public class PosUserAssignment {

    @Id
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    @ManyToOne(optional = false)
    @JoinColumn(name = "admin_user_id", nullable = false)
    private PosAdminUser adminUser;

    @Column(nullable = false)
    private boolean active = true;

    public Long getId() {
        return id;
    }

    public Source getSource() {
        return source;
    }

    public PosAdminUser getAdminUser() {
        return adminUser;
    }

    public boolean isActive() {
        return active;
    }
}

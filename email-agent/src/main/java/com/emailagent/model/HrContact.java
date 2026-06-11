package com.emailagent.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "hr_contacts")
public class HrContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int sno;
    private String name;
    private String email;
    private String title;
    private String company;
}
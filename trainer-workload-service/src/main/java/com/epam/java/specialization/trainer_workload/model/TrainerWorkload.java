package com.epam.java.specialization.trainer_workload.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trainer_workloads")
@CompoundIndexes({
        @CompoundIndex(name = "trainer_fn_ln_idx", def = "{'first_name': 1, 'last_name': 1}")
})
public class TrainerWorkload {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("username")
    private String username;

    @Field("first_name")
    private String firstName;

    @Field("last_name")
    private String lastName;

    @Field("is_active")
    private Boolean isActive;

    @Builder.Default
    @Field("years")
    private List<YearWorkload> years = new ArrayList<>();
}
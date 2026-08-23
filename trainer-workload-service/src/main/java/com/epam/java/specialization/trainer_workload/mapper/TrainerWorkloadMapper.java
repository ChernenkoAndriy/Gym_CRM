package com.epam.java.specialization.trainer_workload.mapper;

import com.epam.java.specialization.trainer_workload.dto.MonthWorkloadDto;
import com.epam.java.specialization.trainer_workload.dto.TrainerWorkloadResponseDto;
import com.epam.java.specialization.trainer_workload.dto.YearWorkloadDto;
import com.epam.java.specialization.trainer_workload.model.MonthWorkload;
import com.epam.java.specialization.trainer_workload.model.TrainerWorkload;
import com.epam.java.specialization.trainer_workload.model.YearWorkload;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TrainerWorkloadMapper {

    @Mapping(source = "isActive", target = "status")
    @Mapping(source = "years", target = "years")
    TrainerWorkloadResponseDto toResponseDto(TrainerWorkload entity);

    @Mapping(source = "yearNumber", target = "year")
    @Mapping(source = "months", target = "months")
    YearWorkloadDto toYearDto(YearWorkload entity);

    List<YearWorkloadDto> toYearDtoList(List<YearWorkload> entities);

    @Mapping(source = "monthNumber", target = "month")
    @Mapping(source = "summaryDuration", target = "trainingSummaryDuration")
    MonthWorkloadDto toMonthDto(MonthWorkload entity);

    List<MonthWorkloadDto> toMonthDtoList(List<MonthWorkload> entities);
}
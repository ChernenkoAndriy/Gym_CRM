Feature: End-to-End cross-service integration between CRM and Trainer Workload Service

  Scenario: Adding a training in CRM updates trainer workload summary asynchronously
    Given a trainer is registered in CRM with first name "Taras", last name "Shevchenko" and specialization "Fitness"
    And a trainee is registered in CRM with first name "Lesya" and last name "Ukrainka"
    When a new training is added in CRM with name "Morning Cardio", date "2026-09-16" and duration 60 minutes
    Then CRM system returns status code 200
    And within 10 seconds trainer workload service contains 60 minutes for the trainer for year 2026 and month 9

  Scenario: Adding multiple trainings accumulates duration properly in workload service
    Given a trainer is registered in CRM with first name "Bohdan", last name "Khmelnytsky" and specialization "Crossfit"
    And a trainee is registered in CRM with first name "Ivan" and last name "Sirko"
    When a new training is added in CRM with name "Morning Session", date "2026-09-16" and duration 45 minutes
    Then CRM system returns status code 200
    When a new training is added in CRM with name "Evening Session", date "2026-09-16" and duration 45 minutes
    Then CRM system returns status code 200
    And within 10 seconds trainer workload service contains 90 minutes for the trainer for year 2026 and month 9

  Scenario: Adding training with invalid duration is rejected by CRM and does not affect workload
    Given a trainer is registered in CRM with first name "Mykhailo", last name "Hrushevsky" and specialization "Fitness"
    And a trainee is registered in CRM with first name "Ivan" and last name "Franko"
    When a new training is added in CRM with name "", date "2026-09-16" and duration 30 minutes
    Then CRM system returns status code 400
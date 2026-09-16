@component @workload
Feature: Trainer Workload Management

  @positive
  Scenario: Successfully retrieve trainer workload summary without filters
    Given a trainer workload exists for "Trainer.Ten" with year 2026, month 8 and duration 90
    And the request is authenticated with service role
    When the client requests workload for trainer "Trainer.Ten"
    Then the response status code is 200
    And the response workload username is "Trainer.Ten"
    And the response workload status is true

  @positive
  Scenario: Successfully retrieve filtered trainer workload summary by year and month
    Given a trainer workload exists for "Trainer.Ten" with year 2026, month 8 and duration 90
    And the request is authenticated with service role
    When the client requests workload for trainer "Trainer.Ten" for year 2026 and month 8
    Then the response status code is 200
    And the response workload username is "Trainer.Ten"
    And the workload year is 2026
    And the workload month is 8

  @negative
  Scenario: Fail to retrieve workload for non-existent trainer
    Given no workload exists for "NonExistent.Trainer"
    And the request is authenticated with service role
    When the client requests workload for trainer "NonExistent.Trainer"
    Then the response status code is 404
    And the error title is "Not Found"

  @nfr @negative
  Scenario: Unauthorized access to workload endpoint without authentication
    When the client requests workload for trainer "Trainer.Ten" without authentication
    Then the response status code is 403

  @positive
  Scenario: Successfully process workload ADD event via Kafka
    When a workload event is published to Kafka with action "ADD", username "Trainer.Active", duration 60
    Then the workload service processes the event for "Trainer.Active"
@component @trainer
Feature: Trainer Profile Management

  @positive
  Scenario: Successfully register a new trainer
    When the user registers a trainer with first name "Maksym" and last name "Semeniuk" and specialization id 1
    Then the response status code is 200
    And the response username is "Maksym.Semeniuk"
    And the response contains a generated password
    And the response contains a valid JWT token

  @negative
  Scenario: Fail to register a trainer with invalid specialization
    When the user registers a trainer with first name "Maksym" and last name "Semeniuk" and specialization id 999
    Then the response status code is 404
    And the error message is "TrainingType not found with ID: 999"

  @negative
  Scenario: Fail to register a trainer with missing fields
    When the user registers an invalid trainer with empty fields
    Then the response status code is 400
    And the error title is "Validation Failed"

  @positive
  Scenario: Successfully get trainer profile by username
    Given the user is authenticated with username "Trainer.Ten" and password "staticPass1"
    When the user requests the trainer profile for "Trainer.Ten"
    Then the response status code is 200
    And the trainer profile first name is "Trainer"
    And the trainer profile last name is "Ten"
    And the trainer specialization is "Yoga"

  @nfr @negative
  Scenario: Unauthorized access to trainer profile without token
    When the user requests the trainer profile for "Trainer.Ten" without token
    Then the response status code is 403

  @positive
  Scenario: Successfully update trainer profile
    Given the user is authenticated with username "Trainer.Ten" and password "staticPass1"
    When the user updates the profile for "Trainer.Ten" with first name "UpdatedTrainer" and last name "Ten" and active status true
    Then the response status code is 200
    And the trainer profile first name is "UpdatedTrainer"
    And the trainer profile last name is "Ten"

  @negative
  Scenario: Fail to update trainer profile with validation errors
    Given the user is authenticated with username "Trainer.Ten" and password "staticPass1"
    When the user updates the profile for "Trainer.Ten" with invalid empty fields
    Then the response status code is 400
    And the error title is "Validation Failed"

  @nfr @negative
  Scenario: Forbidden access when updating another trainer profile
    Given the user is authenticated with username "Trainer.Ten" and password "staticPass1"
    When the user updates the profile for "Trainer.Eleven" with first name "Hacker" and last name "Eleven" and active status true
    Then the response status code is 403
    And the error title is "Forbidden"

  @positive
  Scenario: Successfully deactivate trainer
    Given the user is authenticated with username "Trainer.Ten" and password "staticPass1"
    When the user sets trainer activation status to false for username "Trainer.Ten"
    Then the response status code is 200

  @negative
  Scenario: Fail to set already existing activation status
    Given the user is authenticated with username "Trainer.Ten" and password "staticPass1"
    When the user sets trainer activation status to true for username "Trainer.Ten"
    Then the response status code is 400
    And the error message is "User profile active status is already true"
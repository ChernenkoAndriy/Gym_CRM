@component @trainee
Feature: Trainee Profile Management

  @positive
  Scenario: Successfully register a new trainee
    When the user registers a trainee with first name "Danylo" and last name "Shlapak" and address "Kyiv"
    Then the response status code is 200
    And the response username is "Danylo.Shlapak"
    And the response contains a generated password
    And the response contains a valid JWT token

  @negative
  Scenario: Fail to register a trainee with missing mandatory fields
    When the user registers an invalid trainee with empty fields
    Then the response status code is 400
    And the error title is "Validation Failed"

  @positive
  Scenario: Successfully get trainee profile by username
    Given the user is authenticated with username "Trainee.Ten" and password "staticPass1"
    When the user requests the trainee profile for "Trainee.Ten"
    Then the response status code is 200
    And the profile first name is "Trainee"
    And the profile last name is "Ten"
    And the profile address is "Dnipro, Central St 4"

  @nfr @negative
  Scenario: Unauthorized access to trainee profile without token
    When the user requests the trainee profile for "Trainee.Ten" without token
    Then the response status code is 403

  @nfr @negative
  Scenario: Forbidden access when requesting another trainee profile
    Given the user is authenticated with username "Trainee.Ten" and password "staticPass1"
    When the user requests the trainee profile for "Trainee.Eleven"
    Then the response status code is 403
    And the error title is "Forbidden"

  @positive
  Scenario: Successfully update trainee profile
    Given the user is authenticated with username "Trainee.Ten" and password "staticPass1"
    When the user updates the profile for "Trainee.Ten" with first name "UpdatedTrainee" and last name "Ten" and address "New Kyiv Address" and active status true
    Then the response status code is 200
    And the profile first name is "UpdatedTrainee"
    And the profile address is "New Kyiv Address"

  @negative
  Scenario: Fail to update trainee profile with validation errors
    Given the user is authenticated with username "Trainee.Ten" and password "staticPass1"
    When the user updates trainee profile for "Trainee.Ten" with invalid empty fields
    Then the response status code is 400
    And the error title is "Validation Failed"

  @positive
  Scenario: Successfully change trainee activation status
    Given the user is authenticated with username "Trainee.Ten" and password "staticPass1"
    When the user sets trainee activation status to false for username "Trainee.Ten"
    Then the response status code is 200

  @negative
  Scenario: Fail to set already existing activation status for trainee
    Given the user is authenticated with username "Trainee.Ten" and password "staticPass1"
    When the user sets trainee activation status to true for username "Trainee.Ten"
    Then the response status code is 400
    And the error message is "User profile active status is already true"

  @positive
  Scenario: Successfully get unassigned active trainers
    Given the user is authenticated with username "Trainee.Ten" and password "staticPass1"
    When the user requests unassigned trainers for "Trainee.Ten"
    Then the response status code is 200
    And the trainers list contains "Trainer.Thirteen"

  @positive
  Scenario: Successfully update trainers list
    Given the user is authenticated with username "Trainee.Ten" and password "staticPass1"
    When the user updates trainee trainers list for "Trainee.Ten" with "Trainer.Ten"
    Then the response status code is 200
    And the assigned trainer is "Trainer.Ten"

  @positive
  Scenario: Successfully delete trainee profile
    Given a new registered trainee exists for deletion
    When the user deletes the registered trainee profile
    Then the response status code is 200
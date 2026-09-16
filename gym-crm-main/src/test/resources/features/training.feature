@component @training
Feature: Training Management

  @positive
  Scenario: Successfully add a new training
    Given the user is authenticated with username "Trainee.Ten" and password "staticPass1"
    When the user creates a training with trainee "Trainee.Ten", trainer "Trainer.Eleven", name "Power Crossfit Session", duration 60
    Then the response status code is 200

  @negative
  Scenario: Fail to add training with non-existing trainee
    Given the user is authenticated with username "Trainer.Ten" and password "staticPass1"
    When the user creates a training with trainee "Ghost.Trainee", trainer "Trainer.Ten", name "Ghost Training", duration 45
    Then the response status code is 404
    And the error message is "Trainee not found with username: Ghost.Trainee"

  @negative
  Scenario: Fail to add training with inactive trainee
    Given the user is authenticated with username "Trainer.Ten" and password "staticPass1"
    When the user creates a training with trainee "Trainee.Twelve", trainer "Trainer.Ten", name "Invalid Training", duration 45
    Then the response status code is 403
    And the error message is "Cannot add training: Trainee profile is inactive."

  @negative
  Scenario: Fail to add training with inactive trainer
    Given the user is authenticated with username "Trainee.Ten" and password "staticPass1"
    When the user creates a training with trainee "Trainee.Ten", trainer "Trainer.Twelve", name "Invalid Training", duration 45
    Then the response status code is 403
    And the error message is "Cannot add training: Trainer profile is inactive."

  @negative
  Scenario: Fail to add training with invalid payload
    Given the user is authenticated with username "Trainee.Ten" and password "staticPass1"
    When the user creates an invalid training with empty fields
    Then the response status code is 400
    And the error title is "Validation Failed"

  @positive
  Scenario: Successfully get trainee trainings list with filters
    Given the user is authenticated with username "Trainee.Ten" and password "staticPass1"
    When the user requests filtered trainings for trainee "Trainee.Ten" and type "Yoga"
    Then the response status code is 200
    And the first training name is "Yoga Flow Intermediate"
    And the first training type is "Yoga"
    And the first trainer name is "Trainer Ten"

  @positive
  Scenario: Successfully get trainer trainings list with filters
    Given the user is authenticated with username "Trainer.Ten" and password "staticPass1"
    When the user requests filtered trainings for trainer "Trainer.Ten" and trainee "Trainee.Ten"
    Then the response status code is 200
    And the first training name is "Yoga Flow Intermediate"
    And the first trainee name is "Trainee Ten"

  @positive
  Scenario: Successfully get training types
    Given the user is authenticated with username "Trainee.Ten" and password "staticPass1"
    When the user requests training types
    Then the response status code is 200
    And training types contain "Yoga" and "Crossfit"

  @nfr @negative
  Scenario: Unauthorized access to add training without token
    When the user creates a training with trainee "Trainee.Ten", trainer "Trainer.Eleven", name "Power Crossfit Session", duration 60 without token
    Then the response status code is 403

  @positive
  Scenario: Successfully delete training
    Given the user is authenticated with username "Trainer.Ten" and password "staticPass1"
    When the user deletes training with id 1
    Then the response status code is 200
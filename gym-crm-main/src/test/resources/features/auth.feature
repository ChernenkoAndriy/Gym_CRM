@component @auth
Feature: Authentication and Security

  @positive
  Scenario: Successful login with valid credentials
    When the user logs in with username "Trainee.Ten" and password "staticPass1"
    Then the response status code is 200
    And the response contains a valid JWT token
    And the token type is "Bearer"

  @negative
  Scenario: Failed login with invalid password
    When the user logs in with username "Trainee.Ten" and password "wrong_password"
    Then the response status code is 401

  @negative
  Scenario: Failed login with non-existing user
    When the user logs in with username "Non.Existent" and password "staticPass1"
    Then the response status code is 401

  @negative
  Scenario: Failed login with inactive user
    When the user logs in with username "Trainee.Twelve" and password "staticPass1"
    Then the response status code is 401

  @nfr @negative
  Scenario: Account gets blocked after 3 consecutive failed login attempts
    When the user logs in 3 times with username "Trainee.Ten" and password "wrong_password"
    And the user logs in with username "Trainee.Ten" and password "staticPass1"
    Then the response status code is 423
    And the error title is "Locked"

  @positive
  Scenario: Successful logout invalidates JWT token
    Given the user is authenticated with username "Trainee.Ten" and password "staticPass1"
    When the user logs out
    Then the response status code is 200
    When the user accesses a protected endpoint with the previous token
    Then the response status code is 403
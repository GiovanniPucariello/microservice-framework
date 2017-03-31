Feature: Recipe Management

  Scenario: Add a recipe in system

    Given no previous events
    When addRecipe is called on the Recipe with add-recipe.json
    Then the events are generated with recipe-added.json


  Scenario: Rename a recipe in system

    Given no previous events
    When addRecipe is called on the Recipe with add-recipe.json
    Then the events are generated with recipe-added.json
    When renameRecipe is called on the Recipe with rename-recipe.json
    Then the events are generated with recipe-renamed.json


  Scenario: Remove a recipe in system

    Given no previous events
    When addRecipe is called on the Recipe with add-recipe.json
    Then the events are generated with recipe-added.json
    When removeRecipe is called on the Recipe with remove-recipe.json
    Then the events are generated with recipe-removed.json


  Scenario: Make Cake

    Given no previous events
    When addRecipe is called on the Recipe with add-recipe.json
    Then the events are generated with recipe-added.json
    When makeCake is called on the Recipe with make-cake.json
    Then the events are generated with cake-made.json


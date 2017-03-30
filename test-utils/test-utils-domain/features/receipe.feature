Feature: Recipe Management

  Scenario: Add a recipe in system

    Given no previous events
    When the method addRecipe is called on the aggregate uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe
 """
  {
  "recipeId": "5c5a1d30-0414-11e7-93ae-92361f002671",
  "name": "cheese cake",
  "glutenFree": true,
  "ingredients": [{
  "name": "custard",
  "quantity": 2
  }, {
  "name": "egg",
  "quantity": 6
  }, {
  "name": "sugar",
  "quantity": 500
  }]
  }
  """
    Then the events are generated with following data
  """
  {
  "recipeId": "5c5a1d30-0414-11e7-93ae-92361f002671",
  "name": "cheese cake",
  "glutenFree": true,
  "ingredients": [{
  "name": "custardd",
  "quantity": 2
  }, {
  "name": "egg",
  "quantity": 6
  }, {
  "name": "sugar",
  "quantity": 500
  }],
  "events": [
  "uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded"
  ]
  }
  """

  Scenario: Rename a recipe in system

    Given no previous events
    When the method addRecipe is called on the aggregate uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe
 """
  {
  "recipeId": "5c5a1d30-0414-11e7-93ae-92361f002671",
  "name": "cheese cake",
  "glutenFree": true,
  "ingredients": [{
  "name": "custard",
  "quantity": 2
  }, {
  "name": "egg",
  "quantity": 6
  }, {
  "name": "sugar",
  "quantity": 500
  }]
  }
  """
    Then the events are generated with following data
  """
  {
  "recipeId": "5c5a1d30-0414-11e7-93ae-92361f002671",
  "name": "cheese cake",
  "glutenFree": true,
  "ingredients": [{
  "name": "custard",
  "quantity": 2
  }, {
  "name": "egg",
  "quantity": 6
  }, {
  "name": "sugar",
  "quantity": 500
  }],
  "events": [
  "uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded"
  ]
  }
  """
    When the method renameRecipe is called on the aggregate uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe
  """
  {
  "name": "apple pie"
  }
  """
    Then the events are generated with following data
  """
  {
  "name": "cheese cake",
  "events": [
  "uk.gov.justice.services.example.cakeshop.domain.event.RecipeRenamed"
  ]
  }
  """


  Scenario: Remove a recipe in system

    Given no previous events
    When the method addRecipe is called on the aggregate uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe
 """
  {
  "recipeId": "5c5a1d30-0414-11e7-93ae-92361f002671",
  "name": "cheese cake",
  "glutenFree": true,
  "ingredients": [{
  "name": "custard",
  "quantity": 2
  }, {
  "name": "egg",
  "quantity": 6
  }, {
  "name": "sugar",
  "quantity": 500
  }]
  }
  """
    Then the events are generated with following data
  """
  {
  "recipeId": "5c5a1d30-0414-11e7-93ae-92361f002671",
  "name": "cheese cake",
  "glutenFree": true,
  "ingredients": [{
  "name": "custard",
  "quantity": 2
  }, {
  "name": "egg",
  "quantity": 6
  }, {
  "name": "sugar",
  "quantity": 500
  }],
  "events": [
  "uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded"
  ]
  }
  """

    When the method removeRecipe is called on the aggregate uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe
  """
  {
  }
  """
    Then the events are generated with following data
  """
  {
  "events": [
  "uk.gov.justice.services.example.cakeshop.domain.event.RecipeRemoved"
  ]
  }
  """

  Scenario: Make Cake

    Given no previous events
    When the method addRecipe is called on the aggregate uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe
 """
  {
  "recipeId": "5c5a1d30-0414-11e7-93ae-92361f002671",
  "name": "cheese cake",
  "glutenFree": true,
  "ingredients": [{
  "name": "custard",
  "quantity": 2
  }, {
  "name": "egg",
  "quantity": 6
  }, {
  "name": "sugar",
  "quantity": 500
  }]
  }
  """
    Then the events are generated with following data
  """
  {
  "recipeId": "5c5a1d30-0414-11e7-93ae-92361f002671",
  "name": "cheese cake",
  "glutenFree": true,
  "ingredients": [{
  "name": "custard",
  "quantity": 2
  }, {
  "name": "egg",
  "quantity": 6
  }, {
  "name": "sugar",
  "quantity": 500
  }],
  "events": [
  "uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded"
  ]
  }
  """

    When the method makeCake is called on the aggregate uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe
  """
  {
  "cakeId": "005d6a20-7efe-4697-afa3-2cbb282ec82f"
  }
  """
    Then the events are generated with following data
  """
  {
  "cakeId": "005d6a20-7efe-4697-afa3-2cbb282ec82f",
  "events": [
  "uk.gov.justice.services.example.cakeshop.domain.event.CakeMade"
  ]
  }
  """

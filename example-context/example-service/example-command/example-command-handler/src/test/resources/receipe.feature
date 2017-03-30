# 1) The Event Classes should have Declared Fields and Constructor params in same order
# 2) Json should contain the Events and following json in same order
# 3) Event json object should be in order according to constructor parameters

Feature: Recipe Management

  Scenario: Add a recipe in system

    Given no previous events
    When addRecipe is called on the Recipe
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
[{
	"eventName": "example.recipe-added",
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
}]
"""

  Scenario: Rename a recipe in system

    Given no previous events
    When addRecipe is called on the Recipe
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
[{
	"eventName": "example.recipe-added",
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
}]
  """
    When renameRecipe is called on the Recipe
  """
  {
  "name": "apple pie"
  }
  """
    Then the events are generated with following data
  """
  [{
  	"eventName": "example.recipe-renamed",
  	"recipeId": "5c5a1d30-0414-11e7-93ae-92361f002671",
  	"name": "apple pie"
  }]
  """

  Scenario: Remove a recipe in system

    Given no previous events
    When addRecipe is called on the Recipe
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
[{
	"eventName": "example.recipe-added",
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
}]
  """
    When removeRecipe is called on the Recipe
  """
  {
  }
  """
    Then the events are generated with following data
  """
 [{
  	"eventName": "example.recipe-removed",
  	"recipeId": "5c5a1d30-0414-11e7-93ae-92361f002671"
  }]
  """

  Scenario: Make Cake

    Given no previous events
    When addRecipe is called on the Recipe
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
[{
	"eventName": "example.recipe-added",
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
}]
"""
    When makeCake is called on the Recipe
  """
  {
  "cakeId": "005d6a20-7efe-4697-afa3-2cbb282ec82f"
  }
  """
    Then the events are generated with following data

  """
[{
  	"eventName": "example.cake-made",
  	"cakeId": "005d6a20-7efe-4697-afa3-2cbb282ec82f"
  }]
  """

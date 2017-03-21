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
    Then the following events are generated:
      | uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded |

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
    Then the following events are generated:
      | uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded |

    When the method renameRecipe is called on the aggregate uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe
  """
  {
  "name": "apple pie"
  }
  """
    Then the following events are generated:
      | uk.gov.justice.services.example.cakeshop.domain.event.RecipeRenamed |


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
    Then the following events are generated:
      | uk.gov.justice.services.example.cakeshop.domain.event.RecipeAdded |

    When the method removeRecipe is called on the aggregate uk.gov.justice.services.example.cakeshop.domain.aggregate.Recipe
  """
  {
  }
  """
    Then the following events are generated:
      | uk.gov.justice.services.example.cakeshop.domain.event.RecipeRemoved |

---
description: All possible filters of methods.
---

# Filter keywords

| Keyword          | Example                                                                 | Bson equivalent                   |
| ---------------- | ----------------------------------------------------------------------- | --------------------------------- |
| **(No keyword)** | `findFirstByFirstName(String firstName)`                                | `Filters.eq`                      |
| **Ign**          | `findFirstByFirstNameIgn(String firstName)`                             | `Filters.regex` (`(?i)^[value]$`) |
| **Contains**     | `findFirstByFirstNameContains(String part)`                             | `Filters.regex` (`.*[value].*`)   |
| **GreaterThan**  | `findFirstByBalanceGreaterThan(double balance)`                         | `Filters.gt`                      |
| **LessThan**     | `findFirstByBalanceLessThan(double balance)`                            | `Filters.lt`                      |
| **GreaterEq**    | `findFirstByBalanceGreaterEq(double balance)`                           | `Filters.gte`                     |
| **LessEq**       | `findFirstByBalanceLessEq(double balance)`                              | `Filters.lte`                     |
| **Regex**        | `findFirstByFirstNameRegex(String regex)`                               | `Filters.regex`                   |
| **Regex**        | `findFirstByFirstNameRegex(Pattern pattern)`                            | `Filters.regex`                   |
| **Exists**       | `findFirstByFirstNameExists()`                                          | `Filters.exists`                  |
| **Between**      | `findFirstByBalanceBetween(double greater, double lower)`               | `Filters.gt` + `Filters.lt`       |
| **BetweenEq**    | `findFirstByBalanceBetweenEq(double greaterEquals, double lowerEquals)` | `Filters.gte` + `Filters.lte`     |
| **In**           | `findFirstByCustomerIdIn(List<Integer> customerIdList)`                 | `Filters.in`                      |

You can negate any filter with the keyword `Not`.

| Keyword             | Example                                        | Bson equivalent                                   |
| ------------------- | ---------------------------------------------- | ------------------------------------------------- |
| **Not(No keyword)** | `findFirstByFirstNameNot(String firstName)`    | `Filters.not` + `Filters.eq`                      |
| **NotIgn**          | `findFirstByFirstNameNotIgn(String firstName)` | `Filters.not` + `Filters.regex` (`(?i)^[value]$`) |
| ...                 | _This works with every keyword from above_     | ...                                               |

Filters can also be chained. For this purpose the keyword `And` or the keyword `Or` can be used per method.

_**ATTENTION: The keywords**** ****`And`**** ****and**** ****`Or`**** ****must not be used in the same method!**_

| Keyword                         | Example                                                                         | Bson equivalent                                                      |
| ------------------------------- | ------------------------------------------------------------------------------- | -------------------------------------------------------------------- |
| **Not(No keyword)AndGreaterEq** | `findFirstByFirstNameNotAndBalanceGreaterEq(String firstName, double balance)`  | `Filters.not` + `Filters.eq` && `Filters.gte`                        |
| **IgnAndNotRegex**              | `findFirstByFirstNameIgnAndLastNameNotRegex(String firstName, String lastName)` | `Filters.regex` (`(?i)^[value]$`) && `Filters.not` + `Filters.regex` |
| ...                             | _This works with every keyword from above_                                      | ...                                                                  |

_Note: If a method is declared incorrectly, an exception is usually thrown describing the error. Due to wrong validation checks, this could also occur unintentionally or not at all if the declaration is incorrect._

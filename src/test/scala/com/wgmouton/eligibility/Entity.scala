package com.wgmouton.eligibility


import collection.mutable.Stack
import org.scalatest.flatspec.AnyFlatSpec

class EntitySpec extends AnyFlatSpec {
  /*
  {
"name": "John Smith",
"creditScore": 500
}
Response:
[
{
"cardName": "SuperSaver Card",
"apr": 21.4,
"eligibility": 6.3
},
{
"cardName": "SuperSpender Card",
"apr": 19.2,
"eligibility": 5.0
}
]
  */
  "CSCards" should "return this response given this input" in {
    assert(false)
  }

  it should "be ranked highest no lowest" in {
    assert(false)
  }


  /*
  {
"name": "John Smith",
"score": 341,
"salary": 18500
}
Response:
[
{
"card": "ScoredCard Builder",
"apr": 19.4,
"approvalRating": 0.8
}
]
  */
  "ScoredCards" should "return this response given this input" in {
    assert(false)
  }

  it should "throw NoSuchElementException if unable to cacluate the data" in {
    assert(false)
  }
}

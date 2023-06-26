package com.applaudo.akkalms.dao

import com.applaudo.akkalms.enums.{Currencies, IncomeTypes}
import com.applaudo.akkalms.models.requests.UpdateIncomeRequest
import org.scalamock.scalatest.MockFactory
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec


class IncomeDaoTestSpecs extends AnyWordSpec with MockFactory with Matchers {

  val repository = new IncomeDaoImpl
  "Given a valid personalFinanceId" should {
    "return a list of incomes" in {
      val incomes = repository.findByPersonalFinanceId(9)
      incomes.size shouldBe 2
      incomes.find(_.id == 10).get.name shouldBe "Salary"
      incomes.find(_.id == 11).get.name shouldBe "Extra"
    }
  }

  "Given an already income" should {
    "return an income with its values updated" in {
      val request = UpdateIncomeRequest(Some(10), IncomeTypes.SALARY, 20, Currencies.USD, Some("My note"))
      val income = repository.update(9, request)
      income.amount shouldBe 20
      income.note shouldBe Some("My note")
    }
  }
}

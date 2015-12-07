/**
 * Copyright 2011-2014 eBusiness Information, Groupe Excilys (www.ebusinessinformation.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * Copyright 2011-2014 eBusiness Information, Groupe Excilys (www.excilys.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gatling.http.check.ws

import io.gatling.core.check.DefaultFindCheckBuilder
import io.gatling.core.check.extractor.Extractor
import io.gatling.core.session.Expression
import io.gatling.core.validation.{ Success, Validation }

import scala.concurrent.duration.FiniteDuration

trait WsCheckSupport extends WsCheckDSL {

  implicit def wsDSLStep42Check(step: Step4): WsCheckBuilder = step.message.find.exists
}

trait WsCheckDSL {

  val wsListen = new Step2(false)

  val wsAwait = new Step2(true)

  class Step2(await: Boolean) {

    def within(timeout: FiniteDuration) = new Step3(await, timeout)
  }

  class Step3(await: Boolean, timeout: FiniteDuration) {

    def until(count: Int) = new Step4(await, timeout, UntilCount(count))

    def expect(count: Int) = new Step4(await, timeout, ExpectedCount(count))

    def expect(range: Range) = new Step4(await, timeout, ExpectedRange(range))
  }

  class Step4(await: Boolean, timeout: FiniteDuration, expectation: Expectation) {

    def regex(expression: Expression[String]) = WsRegexCheckBuilder.regex(expression, WsCheckBuilders.extender(await, timeout, expectation))

    def jsonPath(path: Expression[String]) = WsJsonPathCheckBuilder.jsonPath(path, WsCheckBuilders.extender(await, timeout, expectation))

    def jsonpJsonPath(path: Expression[String]) = WsJsonpJsonPathCheckBuilder.jsonpJsonPath(path, WsCheckBuilders.extender(await, timeout, expectation))

    def closeCode = {
      import io.gatling.core.Predef._
      new DefaultFindCheckBuilder[WsCheck, String, String, String](WsCheckBuilders.extender(await, timeout, expectation), WsCheckBuilders.PassThroughMessagePreparer, new Extractor[String, String]() {
        override def name: String = "closeCode"
        override def arity: String = "find"
        override def apply(prepared: String): Validation[Option[String]] = Success(Some(prepared.stripPrefix("closeCode:")))
      })
    }

    val message = WsPlainCheckBuilder.message(WsCheckBuilders.extender(await, timeout, expectation))
  }
}

/**
  * Open Bank Project - API
  * Copyright (C) 2011-2016, TESOBE Ltd
  **
  *This program is free software: you can redistribute it and/or modify
  *it under the terms of the GNU Affero General Public License as published by
  *the Free Software Foundation, either version 3 of the License, or
  *(at your option) any later version.
  **
  *This program is distributed in the hope that it will be useful,
  *but WITHOUT ANY WARRANTY; without even the implied warranty of
  *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *GNU Affero General Public License for more details.
  **
  *You should have received a copy of the GNU Affero General Public License
*along with this program.  If not, see <http://www.gnu.org/licenses/>.
  **
 *Email: contact@tesobe.com
*TESOBE Ltd
*Osloerstrasse 16/17
*Berlin 13359, Germany
  **
 *This product includes software developed at
  *TESOBE (http://www.tesobe.com/)
  * by
  *Simon Redfern : simon AT tesobe DOT com
  *Stefan Bethge : stefan AT tesobe DOT com
  *Everett Sochowski : everett AT tesobe DOT com
  *Ayoub Benali: ayoub AT tesobe DOT com
  *
 */

package code.util

import java.text.SimpleDateFormat
import java.util.{Date, Locale}

import code.api.util.{APIUtil, ErrorMessages}
import code.metrics.OBPUrlDateQueryParam
import code.util.Helper.MdcLoggable
import net.liftweb.common.{Empty, Full}
import org.scalatest.{FeatureSpec, Matchers}

class APIUtilTest extends FeatureSpec with Matchers with MdcLoggable  {
  
  val startDateString = "2010-05-10T01:20:03"
  val startDateStringWrongFormat = "2010-05-10 01:20:03"
  val endDateString = "2017-05-22T01:02:03"
  val endDateStringWrongFormat = "2017-05-22 01:02:03"
  val inputStringDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
  val startDateObject: Date = inputStringDateFormat.parse(startDateString)
  val endDateObject: Date = inputStringDateFormat.parse(endDateString)
  
  feature("test APIUtil.getHttpRequestUrlParams method") 
  {
    scenario("no parameters in the URL") 
    {
      val httpRequestUrl= "/obp/v3.1.0/management/metrics/top-consumers"
      val obpQueryParamPlain = APIUtil.getHttpRequestUrlParams(httpRequestUrl)
      obpQueryParamPlain should be (Full(OBPUrlDateQueryParam(None,None)))
    }

    scenario(s"only one `from_date` in URL") 
    {
      val httpRequestUrl= s"/obp/v3.1.0/management/metrics/top-consumers?from_date=$startDateString"
      val obpQueryParamPlain = APIUtil.getHttpRequestUrlParams(httpRequestUrl)
      obpQueryParamPlain should be (Full(OBPUrlDateQueryParam(Some(startDateObject), None)))
    }

    scenario(s"only one `to_date` in URL") 
    {
      val httpRequestUrl= s"/obp/v3.1.0/management/metrics/top-consumers?to_date=$endDateString"
      val obpQueryParamPlain = APIUtil.getHttpRequestUrlParams(httpRequestUrl)
      obpQueryParamPlain should be (Full(OBPUrlDateQueryParam(None,Some(endDateObject))))
    }

    scenario(s"Both `from_date` and `to_date` in URL") 
    {
      val httpRequestUrl= s"httpRequestUrl = /obp/v3.1.0/management/metrics/top-consumers?from_date=$startDateString&to_date=$endDateString"
      val obpQueryParamPlain = APIUtil.getHttpRequestUrlParams(httpRequestUrl)
      obpQueryParamPlain should be (Full(OBPUrlDateQueryParam((Some(startDateObject)),Some(endDateObject))))
    }
    
    
    scenario(s" exceptions in date format, it should return failure ") 
    {
      val httpRequestUrl= s"httpRequestUrl = /obp/v3.1.0/management/metrics/top-consumers?from_date=$startDateStringWrongFormat&to_date=$endDateString"
      val obpQueryParamPlain = APIUtil.getHttpRequestUrlParams(httpRequestUrl)
      obpQueryParamPlain.toString contains (ErrorMessages.InvalidDateFormat)  should be (true)
      
      val httpRequestUrl2= s"httpRequestUrl = /obp/v3.1.0/management/metrics/top-consumers?from_date=$startDateString&to_date=$endDateStringWrongFormat"
      val obpQueryParamPlain2 = APIUtil.getHttpRequestUrlParams(httpRequestUrl)
      obpQueryParamPlain2.toString contains (ErrorMessages.InvalidDateFormat)  should be (true)
    }
  } 

  feature("test APIUtil.getHttpRequestUrlParam method") 
  {
    scenario("no parameters in the URL") 
    {
      val httpRequestUrl= "/obp/v3.1.0/management/metrics/top-consumers"
      val returnValue = APIUtil.getHttpRequestUrlParam(httpRequestUrl,"from_date")
      returnValue should be (Empty)
    }
    
    scenario(s"only one `from_date` in URL") 
    {
      val httpRequestUrl= s"/obp/v3.1.0/management/metrics/top-consumers?from_date=$startDateString"
      val startdateValue = APIUtil.getHttpRequestUrlParam(httpRequestUrl,"from_date")
      startdateValue should be (Full(s"$startDateString"))
    }
    
    
    scenario(s"Both `from_date` and `to_date` in URL") 
    {
      val httpRequestUrl= s"httpRequestUrl = /obp/v3.1.0/management/metrics/top-consumers?from_date=$startDateString&to_date=$endDateString"
      val startdateValue = APIUtil.getHttpRequestUrlParam(httpRequestUrl,"from_date")
      startdateValue should be (Full(s"$startDateString"))
      val endDateValue = APIUtil.getHttpRequestUrlParam(httpRequestUrl,"to_date")
      endDateValue should be (Full(s"$endDateString"))
      val noneFieldValue = APIUtil.getHttpRequestUrlParam(httpRequestUrl,"none_field")
      noneFieldValue should be (Empty)
    }
    
    scenario(s"test the error case, eg: not proper parameter name") 
    {
      val httpRequestUrl= s"httpRequestUrl = /obp/v3.1.0/management/metrics/top-consumers?from_date=$startDateString&to_date=$endDateString"
      val noneFieldValue = APIUtil.getHttpRequestUrlParam(httpRequestUrl,"none_field")
      noneFieldValue should be (Empty)
    }
  } 
}
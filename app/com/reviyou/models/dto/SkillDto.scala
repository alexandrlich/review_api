package com.reviyou.models.dto


import play.api.libs.json.Json

case class SkillDto(_id: Option[String],
                    skill_name: String)

object SkillDto {

  import com.reviyou.models.Skill._

  implicit val profileDtoFormat = Json.format[SkillDto]
}

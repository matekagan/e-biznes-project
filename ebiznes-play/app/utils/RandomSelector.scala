package utils

import models.Advertisement

import scala.util.Random

object RandomSelector {
  def selectRandom(amount: Int, ads: Seq[Advertisement]): Seq[Advertisement] = {
    if (amount >= ads.size) {
      return ads
    }
    val randomGenerator = new Random
    1.to(amount).map(_ => randomGenerator.nextInt(ads.size))
      .distinct
      .map(ads(_))
  }
}

name := "collaboratr"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  // Bcrypt for password hashing
  "com.github.t3hnar" %% "scala-bcrypt" % "2.3",
  // Scalike-jdbc async extension
  "org.scalikejdbc" %% "scalikejdbc-async" % "[0.3,)",
  "org.scalikejdbc" %% "scalikejdbc-async-play-plugin" % "[0.3,)",
  // Database drivers
  // Synchrounous jdbc driver required for evolutions to work
  "postgresql" % "postgresql" % "9.1-901.jdbc4",
  "com.github.mauricio" %% "postgresql-async" % "0.2.8",
  // Logging
  "ch.qos.logback"  %  "logback-classic"           % "[1.0,)",
  // Webjars
  "org.webjars" %% "webjars-play" % "2.2.0",
  // Webjars libs
  "org.webjars" % "bootstrap" % "3.0.2",
  "org.webjars" % "knockout" % "3.0.0",
  "org.webjars" % "sammy" % "0.7.4",
  "org.webjars" % "jquery" % "2.0.3-1",
  "org.webjars" % "underscorejs" % "1.5.2",
  "org.webjars" % "underscore.string" % "2.3.0",
  "org.webjars" % "chosen" % "0.9.12",
  "org.webjars" % "momentjs" % "2.4.0",
  "org.webjars" % "codemirror" % "3.20"
)

play.Project.playScalaSettings

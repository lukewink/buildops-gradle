package com.lwink.gradle.buildops;

import org.gradle.api.*;
import groovyx.net.http.HTTPBuilder
import static groovyx.net.http.Method.POST
import static groovyx.net.http.ContentType.JSON

/**
 * Plugin implementation that allows for easy interaction with buildops servers.
 */
class BuildopsPlugin implements Plugin<Project> {
  static final String TASK_NAME = "getBuildNumber"
  static final String BUILDOPS_URL = "buildopsUrl"
  static final String BUILDOPS_COMPONENT = "buildopsComponent"
  static final String BUILDOPS_VERSION_BASE = "buildopsVersionBase"
  static final String BUILDOPS_REVISION = "buildopsRevision"
  static final String BUILDOPS_BRANCH = "buildopsBranch"

  /**
   * Define gradle tasks to assist with interacting with buildops servers.
   * 
   * @param project Gradle project calling the plugin.
   */
  def void apply(Project project) {
    project.task(TASK_NAME) {
      description "Get the build number from a buildops server."
      doLast {
        // Check that the required properties have been set
        if (!project.hasProperty(BUILDOPS_URL)) {
          throw new InvalidUserDataException("Project property ${BUILDOPS_URL} is not set")
        }
        if (!project.hasProperty(BUILDOPS_COMPONENT)) {
          throw new InvalidUserDataException("Project property ${BUILDOPS_COMPONENT} is not set")
        }
        if (!project.hasProperty(BUILDOPS_VERSION_BASE)) {
          throw new InvalidUserDataException("Project property ${BUILDOPS_VERSION_BASE} is not set")
        }

        HTTPBuilder httpBuilder = new HTTPBuilder(project.buildopsUrl)
        httpBuilder.request(POST, JSON) {
          uri.path = "/api/builds"

          // Add the required fields component and version_base
          body = [component: project.getProperty(BUILDOPS_COMPONENT), 
                  version_base: project.getProperty(BUILDOPS_VERSION_BASE)]

          // Now add the optional fields
          if (project.hasProperty(BUILDOPS_REVISION)) {
            body.revision = project.getProperty(BUILDOPS_REVISION)
          }
          if (project.hasProperty(BUILDOPS_BRANCH)) {
            body.branch = project.getProperty(BUILDOPS_BRANCH)
          }

          response.success = { resp, build ->
            assert resp.statusLine.statusCode == 201
            project.ext.set("buildopsBuildNumber", build.number)
          } 
        }
      }
    }
  }
}



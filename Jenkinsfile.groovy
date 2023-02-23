node('built-in') {
  stage ('BlazeMeter test'){
    blazeMeterTest(
      jobApiKey:'e7abfc8f0f92f0a1c0e69b80',
      serverUrl:'https://a.blazemeter.com',
      testId:'53841',
      notes:'',
      sessionProperties:'',
      jtlPath:'',
      junitPath:'',
      getJtl:false,
      getJunit:false
    )
  }
}

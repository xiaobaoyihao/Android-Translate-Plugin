package com.fzm.translate

import groovy.json.JsonSlurper
import groovy.xml.MarkupBuilder
import org.gradle.api.Plugin
import org.gradle.api.Project

class TranslatePlugin implements Plugin<Project> {

    def mJsonSlurper = new JsonSlurper()

    @Override
    void apply(Project project) {


        project.task('translate') {

            doLast {

                def valuesDir = new File(project.getProjectDir().getPath() + "/src/main/res/values")
                def enValuesDir = new File(project.getProjectDir().getPath() + "/src/main/res/values-en")

                valuesDir.traverse { file ->

                    if (file.file && file.name.contains("strings")) {

                        //读取汉字xml文件
                        Map<String, String> map = parseStringXml(file)

                        //对汉字进行翻译处理
                        Map<String, String> newMap = new HashMap<>()
                        map.each { entry ->

                            String newValue = translate(entry.value.toString())
                            newMap.put(entry.key.toString(), newValue.toString())
                        }

                        //写到新文件中
                        writeFile(new File(enValuesDir, file.name), newMap)
                    }
                }
            }
        }
    }

    private String translate(String query) {


        String response = com.github.kevinsawicki.http.HttpRequest.post("https://openapi.youdao.com/api",
                http.TranslateHttp.buildRequest(query),
                true).body()

        //Json 解析
        def responseBean = mJsonSlurper.parseText(response)

        String result = ''
        if (responseBean
                && responseBean.web
                && responseBean.web.get(0).value) {
            result = responseBean.web.get(0).value.get(0)
        }

        return result
    }


    private Map<String, String> parseStringXml(def file) {

        Map<String, String> map = new HashMap<>()

        def xmlString = new XmlSlurper().parse(file)
        xmlString.string.each {
            map.put("${it.@name}".toString(), "${it}".toString())
        }

        return map
    }

    private void writeFile(File saveFile, Map<String, String> map) {

        /**
         * 如果对应文件存在，则追加相关数据
         * 否则创建文件即可
         */
        if (saveFile.exists()) {

            Map<String, String> existMap = parseStringXml(saveFile)


            def writer = new StringWriter()
            def xml = new MarkupBuilder(writer)

            xml.resources() {

                existMap.each { entry ->

                    string(name: "$entry.key", "$entry.value")
                }


                writer.write("\n\n  <!--ADD STRING START-->")
                map.each { entry ->

                    if (!existMap.containsKey("$entry.key".toString())) {
                        string(name: "$entry.key", "$entry.value")
                    }
                }
                writer.write("\n  <!--ADD STRING END-->\n")
            }

            FileWriter fw = new FileWriter(saveFile, false)
            fw.write(writer.toString())
            fw.close()

        } else {
            //create file

            def writer = new StringWriter()
            def xml = new MarkupBuilder(writer)

            xml.resources() {

                map.each { entry ->
                    string(name: "$entry.key", "$entry.value")
                }
            }

            FileWriter fw = new FileWriter(saveFile)
            fw.write(writer.toString())
            fw.close()
        }
    }
}
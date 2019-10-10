// Exported from:        http://kubuntu:5516/#/templates/Foldera4e502533f9142e2bb6034b566242c36-Release9835d9aa0736415ab9e9b47229c8ff2e/releasefile
// XL Release version:   9.0.6
// Date created:         Thu Oct 10 10:40:36 CEST 2019

xlr {
  template('Provision new infrastructure - java bdd project') {
    folder('Terraform')
    variables {
      listBoxVariable('aws_region') {
        required false
        showOnReleaseStart false
        label 'AWS Region'
        description 'Región de AWS en la que se crearán los recursos y la infraestructura'
        possibleValues 'us-east-1', 'us-east-2', 'us-west-1', 'us-west-2', 'eu-west-1', 'eu-west-2', 'eu-west-3'
        value 'us-east-1'
      }
      listBoxVariable('environment') {
        required false
        showOnReleaseStart false
        label 'Entorno'
        description 'Entorno objeto de la petición de la infraestructura'
        possibleValues 'dev', 'pre', 'pro'
        value 'dev'
      }
      listBoxVariable('instance_type') {
        required false
        showOnReleaseStart false
        label 'Tipo de instancia EC2'
        description 'Tipo de las instancias EC2 que se crearán. Para entornos productivos se recomiendan instancias de mayor capacidad.'
        possibleValues 't2.nano', 't2.micro', 't2.small', 't2.medium', 't2.large', 't2.xlarge', 't2.2xlarge'
        value 't2.micro'
      }
      stringVariable('project_name') {
        required false
        showOnReleaseStart false
        label 'Nombre del proyecto'
        description 'Nombre del proyecto a crear. Debe existir una aplicación del mismo nombre en XL Deploy bajo Applications/Infrastructures/Terraform'
        value 'calculator'
      }
      stringVariable('public_key_path') {
        required false
        showOnReleaseStart false
        label 'Clave pública'
        description 'Clave pública que se instalará en los servidores remotos para acceder a ellos. Debe estar accesible desde la máquina que aplique las templates de Terraform.'
        value '/home/jcla/.ssh/id_rsa.pub'
      }
      stringVariable('private_key_path') {
        required false
        showOnReleaseStart false
        label 'Clave privada'
        description 'Clave privada para acceder a las instancias EC2. Debe estar accesible desde la máquina de XL Deploy.'
        value '/home/jcla/.ssh/id_rsa'
      }
      listVariable('versions_infrastructure_availables') {
        required false
        showOnReleaseStart false
        label 'Versiones de infraestructura disponibles'
        description 'Versiones de infraestructura disponibles'
      }
      listBoxVariable('version_infrastructure_selected') {
        required false
        showOnReleaseStart false
        label 'Versión de la infraestructura seleccionada'
        possibleValues variable('versions_infrastructure_availables')
      }
      listBoxVariable('version_provisioning') {
        required false
        showOnReleaseStart false
        label 'Versión de los playbooks de Ansible para provisionar la infraestructura'
        possibleValues '3.0.0', '2.0.0', '1.0.0'
        value '3.0.0'
      }
      stringVariable('ip_front') {
        required false
        showOnReleaseStart false
        label 'IP de la máquina de front'
        description 'IP obtenida de forma dinámica de las instancias EC2 creadas'
      }
      stringVariable('ip_bdd') {
        required false
        showOnReleaseStart false
        label 'IP de la máquina de bdd'
        description 'IP obtenida de forma dinámica de las instancias EC2 creadas'
      }
      listVariable('list_tags') {
        required false
        showOnReleaseStart false
      }
      listBoxVariable('tag_ansible_selected') {
        required false
        showOnReleaseStart false
        label 'Versiones de playbooks disponibles'
        possibleValues variable('list_tags')
      }
      stringVariable('user_tomcat') {
        required false
        showOnReleaseStart false
        label 'Usuario administrador de Tomcat'
        value 'admin'
      }
      passwordVariable('password_tomcat') {
        required false
        showOnReleaseStart false
        label 'Password para el administrador de Tomcat'
      }
    }
    description '# Template para el provisionamiento de nueva infraestructura en AWS\n' +
                '\n' +
                'Esta template se encargar del provisionamiento de una nueva infraestructura en AWS'
    scheduledStartDate Date.parse("yyyy-MM-dd'T'HH:mm:ssZ", '2019-08-08T09:00:00+0200')
    dueDate Date.parse("yyyy-MM-dd'T'HH:mm:ssZ", '2019-09-30T17:44:41+0200')
    scriptUsername 'admin'
    scriptUserPassword '{aes:v0}RQbNScDkbrKHuHYjNNOPZ7mnXxpa1z8wJVr93/VUa3g='
    phases {
      phase('OBTENCIÓN DE DATOS') {
        color '#0099CC'
        tasks {
          userInput('Parámetros de configuración e infraestructura') {
            description '## Datos para la creación de la infraestructura\n' +
                        'Es necesario proporcionar una serie de datos para poder realizar el provisioning de la infraestructura en AWS. **Será necesario facilitar los siguientes datos**.'
            owner 'ahortalq'
            team 'Desarrollo'
            variables {
              variable 'aws_region'
              variable 'environment'
              variable 'instance_type'
              variable 'project_name'
              variable 'public_key_path'
              variable 'private_key_path'
            }
          }
          custom('Verificación de existencia de proyecto de infraestructura para ${project_name}') {
            description '## Verificación de existencia de proyecto de infraestructura para ${project_name}\n' +
                        '\n' +
                        'Será el proyecto con las templates de Terraform que se encargará de hacer el provisioning  de la infraestructura.'
            owner 'charo'
            team 'Automatización'
            script {
              type 'xld.DoesCIExist'
              server 'XL Deploy'
              ciID 'Applications/Infrastructures/Terraform/infrastructure-${project_name}'
              throwOnFail true
              exists false
            }
          }
          custom('Obtención de las versiones disponibles para ${project_name}') {
            description '## Obtención de las versiones disponibles para ${project_name}\n' +
                        '\n' +
                        'Obtendremos un listado con las **distintas versiones de infraestructura disponibles** para el proyecto ${project_name}'
            owner 'jcla'
            team 'Cloud'
            script {
              type 'xld.GetAllVersions'
              server 'XL Deploy'
              applicationId 'Applications/Infrastructures/Terraform/infrastructure-${project_name}'
              packageIds variable('versions_infrastructure_availables')
            }
          }
          userInput('Selección de versión de infraestructura') {
            description '## Selección de versión de la infraestructura\n' +
                        'Es necesario proporcionar la versión de la infraestructura que se desea aplicar. Una vez seleccionada, se tendrán:\n' +
                        '\n' +
                        '* Recursos creados en la región **${aws_region}**\n' +
                        '* Instancias del tipo **${instance_type}**\n' +
                        '* Entorno de tipo **${environment}**\n' +
                        '* Para el proyecto **${project_name}**'
            owner 'antonio'
            team 'Provisioning'
            variables {
              variable 'version_infrastructure_selected'
            }
          }
        }
      }
      phase('CREACIÓN DE ENTORNO') {
        color '#D94C3D'
        tasks {
          custom('Creación de infraestructura base en XLD') {
            description '## Creación de recursos en XLD\n' +
                        '\n' +
                        'Se invocará a la ejecución del script remoto `https://raw.githubusercontent.com/jclopeza/xlr-scripts/master/createXLDResourcesTerraformModuleJavaBddProject.py`\n' +
                        '\n' +
                        'Este script se ejecutará con el CLI para la creación de los recursos necesarios en XLD'
            owner 'cvalero'
            team 'Automatización'
            script {
              type 'xld.cliUrl'
              cli 'xl-deploy-9.0.5-cli'
              scriptUrl 'https://raw.githubusercontent.com/jclopeza/xlr-scripts/master/createXLDResourcesTerraformModuleJavaBddProject.py'
              options '${environment} ${project_name} ${aws_region} ${instance_type} ${private_key_path} ${public_key_path}'
            }
          }
          manual('Validación de entorno en XLD') {
            description '### Recursos necesarios listos para despliegue de infraestructura\n' +
                        'Se han facilitado los siguientes datos para la creación de una nueva infraestructura:\n' +
                        '\n' +
                        '1. **Environment:** ${environment}\n' +
                        '2. **Proyecto:** ${project_name}\n' +
                        '3. **Región AWS:** ${aws_region}\n' +
                        '4. **Tipo de instancias:** ${instance_type}\n' +
                        '5. **Claves pública y privada:** ${public_key_path} y ${private_key_path}\n' +
                        '\n' +
                        '### Entorno de despliegue\n' +
                        'Se han creado en XL Deploy el entorno necesario para hacer el despliegue.\n' +
                        '\n' +
                        '#### Nombre del entorno de despliegue:\n' +
                        'Environments/infrastructure-${project_name}/infrastructure-${project_name}-${environment}/infrastructure-${project_name}-${environment}\n' +
                        '\n' +
                        '### Valide que se han creado correctamente los recursos en XLD'
            owner 'jccobo'
            team 'Cloud'
          }
          gate('Validación de solicitud y aprobación del equipo de seguridad') {
            description '### Validación de solicitud\n' +
                        'El equipo de desarrollo solicita la creación de la siguiente infraestructura:\n' +
                        '\n' +
                        '1. **Environment:** ${environment}\n' +
                        '2. **Proyecto:** ${project_name}\n' +
                        '3. **Región AWS:** ${aws_region}\n' +
                        '4. **Tipo de instancias:** ${instance_type}\n' +
                        '5. **Claves pública y privada:** ${public_key_path} y ${private_key_path}\n' +
                        '\n' +
                        '### El entorno de despliegue ya ha sido validado\n' +
                        'Environments/infrastructure-${project_name}/infrastructure-${project_name}-${environment}/infrastructure-${project_name}-${environment}'
            owner 'vsalguero'
            team 'Seguridad'
            precondition '"${environment}" == "pro"'
            locked true
          }
          manual('eliminar') {
            
          }
        }
      }
      phase('CREACIÓN DE INFRAESTRUCTURA') {
        color '#08B153'
        tasks {
          custom('Creación de infraestructura en AWS') {
            owner 'ahartman'
            team 'Provisioning'
            script {
              type 'xldeploy.Deploy'
              server 'XL Deploy'
              retryCounter 'currentContinueRetrial':'0','currentPollingTrial':'0'
              deploymentPackage '${version_infrastructure_selected}'
              deploymentEnvironment 'Environments/infrastructure-${project_name}/infrastructure-${project_name}-${environment}/infrastructure-${project_name}-${environment}'
            }
          }
          parallelGroup('Obtención de las IPs remotas') {
            owner 'amateos'
            team 'Desarrollo'
            tasks {
              custom('IP de la máquina front') {
                owner 'amateos'
                team 'Desarrollo'
                script {
                  type 'xld.GetCIStringProperty'
                  server 'XL Deploy'
                  ciID 'Infrastructure/${project_name}-${environment}-front'
                  ciPropertyName 'address'
                  ciPropertyValue variable('ip_front')
                }
              }
              custom('IP de la máquina bdd') {
                owner 'amateos'
                team 'Desarrollo'
                script {
                  type 'xld.GetCIStringProperty'
                  server 'XL Deploy'
                  ciID 'Infrastructure/${project_name}-${environment}-bdd'
                  ciPropertyName 'address'
                  ciPropertyValue variable('ip_bdd')
                }
              }
            }
          }
          sequentialGroup('Instalación de python en las máquinas remotas') {
            owner 'ycobo'
            team 'Automatización'
            tasks {
              custom('Instalar python máquina front') {
                owner 'ycobo'
                team 'Automatización'
                script {
                  type 'remoteScript.Unix'
                  script 'ssh -o "StrictHostKeyChecking=no" ubuntu@${ip_front} -i ${private_key_path} "sudo apt-get update && sudo apt-get -y install python-minimal"'
                  remotePath '/tmp'
                  address 'localhost'
                  username 'jcla'
                  password '{aes:v0}mBSR3uhfuyrIkG2aiFvzSRoPqB0s872U+AnciZXpyBA='
                }
              }
              custom('Instalar python máquina bdd') {
                owner 'ycobo'
                team 'Automatización'
                script {
                  type 'remoteScript.Unix'
                  script 'ssh -o "StrictHostKeyChecking=no" ubuntu@${ip_bdd} -i ${private_key_path} "sudo apt-get update && sudo apt-get -y install python-minimal"'
                  remotePath '/tmp'
                  address 'localhost'
                  username 'jcla'
                  password '{aes:v0}Pmwg75v1JOteonumzFLZ7NR0iaPO4LHLRWyR1QVeu0c='
                }
              }
            }
          }
        }
      }
      phase('PROVISIONING') {
        color '#991C71'
        tasks {
          sequentialGroup('Selección de versión de playbooks de Ansible a ejecutar') {
            owner 'isanchez'
            team 'Provisioning'
            tasks {
              script('Obtención de las versiones disponibles de playbooks') {
                description '### Este script se encarga de consultar a Github las versiones disponibles de los playbooks para el provisioning'
                owner 'isanchez'
                team 'Provisioning'
                script (['''\
import json
gitServer = 'https://api.github.com'
request = HttpRequest({'url': gitServer})
response = request.get('/repos/jclopeza/playbooks-provisioning/tags', contentType='application/json')
listTags = []
data = json.loads(response.response)
for i in data:
    listTags = listTags + [i['name']]
releaseVariables['list_tags'] = listTags

# Nueva version
# import urllib2
# import json
# import base64
# req = urllib2.Request('https://api.github.com/repos/jclopeza/playbooks-provisioning/tags')
# listTags = []
# req.add_header('Content-Type','application/json')
# response = urllib2.urlopen(req)
# data = json.loads(response.read())
# for i in data:
#     listTags = listTags + [i['name']]
# releaseVariables['list_tags'] = listTags
'''])
              }
              userInput('Selección de versión de provisioning') {
                description '### Máquinas a provisionar\n' +
                'Se van a provisionar las máquinas:\n' +
                '\n' +
                '#### ${project_name}-${environment}-front con IP = ${ip_front}\n' +
                '#### ${project_name}-${environment}-bdd con IP = ${ip_bdd}\n' +
                '\n' +
                '### Versión de los playbooks\n' +
                'Debe seleccionar la versión de los playbooks que se van a utilizar para hacer el provisioning del nuevo entorno creado.\n' +
                '\n' +
                '#### Debe seleccionar entre una de las versiones que hay disponibles para el proyecto ${project_name}'
                owner 'isanchez'
                team 'Provisioning'
                variables {
                  variable 'tag_ansible_selected'
                }
              }
              custom('Checkout a la versión de los playbooks seleccionada') {
                owner 'isanchez'
                team 'Provisioning'
                script {
                  type 'remoteScript.Unix'
                  script 'cd /tmp && rm -fr playbooks-provisioning\n' +
                  'git clone https://github.com/jclopeza/playbooks-provisioning.git\n' +
                  'cd playbooks-provisioning\n' +
                  'git checkout ${tag_ansible_selected}'
                  address 'localhost'
                  username 'jcla'
                  password '{aes:v0}amyWc1Rog2jQNy3OEZ+XhG1czWVl3h359VpAaFQ1eEg='
                }
              }
            }
          }
          parallelGroup('Provisioning de instancias') {
            owner 'mvega'
            team 'Desarrollo'
            tasks {
              custom('Provisioning de la instancia-front EC2 en Amazon') {
                description '### Ejecución del script de provisioning\n' +
                'Este script provisionará la instancia EC2 que encargará de la parte front.'
                owner 'mvega'
                team 'Desarrollo'
                script {
                  type 'ansible.RunPlaybook'
                  host 'ansible-machine-control'
                  playbookPath '/tmp/playbooks-provisioning/playbook-front.yml'
                  cmdParams '-u ubuntu -i "${ip_front}," --private-key "${private_key_path}" --ssh-common-args="-o StrictHostKeyChecking=no" -e "public_key_path=${public_key_path}"'
                }
              }
              custom('Provisioning de la instancia-bdd EC2 en Amazon') {
                description '### Ejecución del script de provisioning\n' +
                'Este script provisionará la instancia EC2 que encargará de la parte bdd.'
                owner 'mvega'
                team 'Desarrollo'
                script {
                  type 'ansible.RunPlaybook'
                  host 'ansible-machine-control'
                  playbookPath '/tmp/playbooks-provisioning/playbook-bdd.yml'
                  cmdParams '-u ubuntu -i "${ip_bdd}," --private-key "${private_key_path}" --ssh-common-args="-o StrictHostKeyChecking=no"'
                }
              }
            }
          }
          custom('Creación de entorno en XLD') {
            description '## Creación de recursos en XLD\n' +
            '\n' +
            'Se invocará a la ejecución del script remoto `https://raw.githubusercontent.com/jclopeza/xlr-scripts/master/createXLDResourcesTerraformModuleJavaBddProjectContainers.py`\n' +
            '\n' +
            'Este script se ejecutará con el CLI para la creación de nuevos containers en XLD y un nuevo entorno de despliegue.'
            owner 'jsalguero'
            team 'Cloud'
            script {
              type 'xld.cliUrl'
              cli 'xl-deploy-9.0.5-cli'
              scriptUrl 'https://raw.githubusercontent.com/jclopeza/xlr-scripts/master/createXLDResourcesTerraformModuleJavaBddProjectContainers.py'
              options '${environment} ${project_name} ${ip_front} ${ip_bdd}'
            }
          }
          notification('Notificación a desarrollo de nuevo entorno disponible') {
            description '### Notificación a desarrollo de nuevo entorno disponible\n' +
            'Notificación al equipo de desarrollo de que se ha creado el nuevo entorno para que puedan desplegarse las aplicaciones correspondientes.'
            owner 'jmurcia'
            team 'Seguridad'
            addresses 'oscar@gmail.com'
            subject 'Entorno disponible para ${project_name} y entorno ${environment}'
            body '### Entorno creado para el proyecto ${project_name} y entorno ${environment}\n' +
            '\n' +
            'Se ha creado un nuevo entorno en XL Deploy para que pueda desplegar su aplicación.'
          }
        }
      }
      phase('COMUNICACIÓN Y VALIDACIÓN') {
        color '#FD8D10'
        tasks {
          sequentialGroup('Generación de reports') {
            owner 'agiraldo'
            team 'Seguridad'
            tasks {
              custom('Generación de gráfico con la infraestructura') {
                owner 'agiraldo'
                team 'Seguridad'
                script {
                  type 'remoteScript.Unix'
                  script '#!/bin/bash\n' +
                  'TERRAFORMDIR=/var/opt/xebialabs/terraform-states/${project_name}-${environment}\n' +
                  'HTMLDIR=/var/www/html/${project_name}-${environment}\n' +
                  '[ -d $TERRAFORMDIR ] || exit 1\n' +
                  '[ -d $HTMLDIR ] || mkdir -p $HTMLDIR\n' +
                  '[ -f $HTMLDIR/graph.svg ] && rm $HTMLDIR/graph.svg\n' +
                  '[ -f $HTMLDIR/index.html ] && rm $HTMLDIR/index.html\n' +
                  'cd $TERRAFORMDIR\n' +
                  'cd -- "$(find . -name terraform.tfstate -type f -printf \'%h\' -quit)"\n' +
                  'terraform graph | dot -Tsvg > $HTMLDIR/graph.svg'
                  address 'localhost'
                  username 'jcla'
                  password '{aes:v0}skBXgx+lKysSc6Ww60h/k6vJ+VNhJZPcc85dUxbR3+Q='
                }
              }
              custom('Generación de html') {
                owner 'agiraldo'
                team 'Seguridad'
                script {
                  type 'remoteScript.Unix'
                  script 'python3 generateTerraformHtmlReport.py ${environment} ${project_name} ${aws_region} ${instance_type} ${version_infrastructure_selected} ${tag_ansible_selected}'
                  remotePath '/home/jcla/Projects/xlr-scripts'
                  address 'localhost'
                  username 'jcla'
                  password '{aes:v0}VHHg1MKrLeiziWRAK4/CFGNScdKdQ3dC6EwEPIwgQr8='
                }
              }
            }
          }
          manual('Validación de conexión a hosts remotos') {
            description '### Validación de conexión a hosts remotos\n' +
            'Se han creado nuevas instancias EC2 en XL Deploy. Debe acceder para comprobar que hay conectividad con las claves proporcionadas.\n' +
            '\n' +
            '#### Los nombres de las máquinas son "${project_name}-${environment}-*"'
            owner 'isanchez'
            team 'Provisioning'
          }
          notification('Infraestructura creada en AWS') {
            description '### Notificación de infraestructura creada\n' +
            'Notificación al equipo de operaciones de que se ha creado la estructura y de que debe acometer ciertas operaciones.'
            owner 'isanchez'
            team 'Provisioning'
            addresses 'ramon@gmail.com'
            subject 'Infraestructura provisionada para el proyecto ${project_name} y entorno ${environment}'
            body '### Infraestructura provisionada para el proyecto ${project_name} y entorno ${environment}\n' +
            '\n' +
            'Se ha creado nueva infraestructura en AWS. Recuerde que:\n' +
            '\n' +
            '* Debe habilitar la monitorización\n' +
            '* Debe establecer alertas de consumo\n' +
            '* Debe notificar cualquier anomalía o corte de servicio a la dirección sistemas@gmail.com\n' +
            '\n' +
            'Estos son los datos relacionados con la infraestructura:\n' +
            '\n' +
            '1. **Environment:** ${environment}\n' +
            '2. **Proyecto:** ${project_name}\n' +
            '3. **Región AWS:** ${aws_region}\n' +
            '4. **Tipo de instancias:** ${instance_type}\n' +
            '5. **Claves pública y privada:** ${public_key_path} y ${private_key_path}\n' +
            '\n' +
            'También puedes consultar el [gráfico de la estructura creada](http://localhost/${project_name}-${environment}).'
          }
        }
      }
    }
    extensions {
      dashboard('Dashboard') {
        parentId 'Applications/Foldera4e502533f9142e2bb6034b566242c36/Release9835d9aa0736415ab9e9b47229c8ff2e'
        owner 'admin'
        tiles {
          releaseProgressTile('Release progress') {
            
          }
          releaseSummaryTile('Release summary') {
            
          }
          resourceUsageTile('Resource usage') {
            
          }
          timelineTile('Release timeline') {
            
          }
          releaseHealthTile('Release health') {
            
          }
        }
      }
    }
    
  }
}
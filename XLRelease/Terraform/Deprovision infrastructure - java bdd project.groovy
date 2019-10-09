// Exported from:        http://kubuntu:5516/#/templates/Foldera4e502533f9142e2bb6034b566242c36-Release9fdf3d7607c14398b936a3c1a40e6c92/releasefile
// XL Release version:   9.0.6
// Date created:         Wed Oct 09 16:52:19 CEST 2019

xlr {
  template('Deprovision infrastructure - java bdd project') {
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
      stringVariable('last_version_deployed_front') {
        required false
        showOnReleaseStart false
      }
      stringVariable('last_version_deployed_webservices') {
        required false
        showOnReleaseStart false
      }
    }
    description '# Template para el provisionamiento de nueva infraestructura en AWS\n' +
                '\n' +
                'Esta template se encargar del provisionamiento de una nueva infraestructura en AWS'
    scheduledStartDate Date.parse("yyyy-MM-dd'T'HH:mm:ssZ", '2019-09-30T16:44:40+0200')
    dueDate Date.parse("yyyy-MM-dd'T'HH:mm:ssZ", '2019-09-30T17:44:40+0200')
    scriptUsername 'admin'
    scriptUserPassword '{aes:v0}8j7U6EAwgnJnZeaFfffcQ+DTBzjjEvx3m9kyh1WKe7s='
    phases {
      phase('OBTENCIÓN DE DATOS') {
        color '#0099CC'
        tasks {
          userInput('Parámetros de configuración e infraestructura') {
            description '## Datos para eliminar la infraestructura\n' +
                        'Es necesario proporcionar una serie de datos para poder realizar el deprovisioning de la infraestructura en AWS. **Será necesario facilitar los siguientes datos**.'
            variables {
              variable 'environment'
              variable 'project_name'
            }
          }
        }
      }
      phase('UNDEPLOY APLICACIONES') {
        color '#D94C3D'
        tasks {
          sequentialGroup('Undeploy de las aplicaciones desplegadas') {
            tasks {
              custom('Parte front') {
                script {
                  type 'xldeploy.Undeploy'
                  server 'XL Deploy'
                  retryCounter 'currentContinueRetrial':'0','currentPollingTrial':'0'
                  deployedApplication 'Environments/application-${project_name}/application-${project_name}-${environment}/application-${project_name}-${environment}/front'
                }
              }
              custom('Parte webservices') {
                script {
                  type 'xldeploy.Undeploy'
                  server 'XL Deploy'
                  retryCounter 'currentContinueRetrial':'0','currentPollingTrial':'0'
                  deployedApplication 'Environments/application-${project_name}/application-${project_name}-${environment}/application-${project_name}-${environment}/webservices'
                }
              }
            }
          }
        }
      }
      phase('ELIMINACIÓN DE RECURSOS') {
        color '#08B153'
        tasks {
          custom('Eliminamos entorno de despliegue') {
            script {
              type 'xld.DeleteCI'
              server 'XL Deploy'
              ciID 'Environments/application-${project_name}'
            }
          }
          sequentialGroup('Eliminamos elementos de infraestructura') {
            tasks {
              custom('Eliminamos mysql-cli') {
                script {
                  type 'xld.DeleteInfrastructure'
                  server 'XL Deploy'
                  ci_id 'Infrastructure/${project_name}-${environment}-bdd/mysql-cli'
                }
              }
              custom('Eliminamos axis2') {
                script {
                  type 'xld.DeleteInfrastructure'
                  server 'XL Deploy'
                  ci_id 'Infrastructure/${project_name}-${environment}-front/axis2'
                }
              }
              custom('Eliminamos smokeTest') {
                script {
                  type 'xld.DeleteInfrastructure'
                  server 'XL Deploy'
                  ci_id 'Infrastructure/${project_name}-${environment}-front/smokeTest'
                }
              }
              custom('Eliminamos tomcat') {
                script {
                  type 'xld.DeleteInfrastructure'
                  server 'XL Deploy'
                  ci_id 'Infrastructure/${project_name}-${environment}-front/tomcat'
                }
              }
            }
          }
        }
      }
      phase('DEPROVISION AWS') {
        color '#0099CC'
        tasks {
          custom('Deprovision AWS') {
            script {
              type 'xldeploy.Undeploy'
              server 'XL Deploy'
              retryCounter 'currentContinueRetrial':'0','currentPollingTrial':'0'
              deployedApplication 'Environments/infrastructure-${project_name}/infrastructure-${project_name}-${environment}/infrastructure-${project_name}-${environment}/infrastructure-${project_name}'
            }
          }
          custom('Eliminamos entorno AWS') {
            script {
              type 'xld.DeleteCI'
              server 'XL Deploy'
              ciID 'Environments/infrastructure-${project_name}/infrastructure-${project_name}-${environment}'
            }
          }
        }
      }
    }
    extensions {
      dashboard('Dashboard') {
        parentId 'Applications/Foldera4e502533f9142e2bb6034b566242c36/Release9fdf3d7607c14398b936a3c1a40e6c92'
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
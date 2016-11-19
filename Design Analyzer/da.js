var calculateMetrics = function(projectFile) {
   var tools = require('./tools.js');
   var project = tools.getModel(projectFile);
   var classes = tools.collectElements(project, "_type", "UMLClass");
   var interfaces = tools.collectElements(project, "_type", "UMLInterface");

   var bidirectionalClassArray = new Array(0);

   /*
   			Calculate total no of classes
   */
   var totalNoOfClasses = classes.length + interfaces.length;
   console.log("totalNoOfClasses = " + totalNoOfClasses);

   for(var i in classes)
   {
   		var associations = tools.collectElements(classes[i], "_type", "UMLAssociation");
   		for(var j in associations)
   		{
   			if(associations[j].end1.navigable == true && associations[j].end2.navigable == true)
   				bidirectionalClassArray.push(associations[j].end1.reference.$ref + "_" + associations[j].end2.reference.$ref);
   		}
   }

   console.log("\t\t\tResponsibility\t\tStability\t\tDeviance");

   /*
   			Calculating design metrics for classes
   */
   for(var i in classes)
   {
   		var instabilityArray = new Array(0);
   		var responsibilityArray = new Array(0);

   		//console.log("Current class --> " + classes[i].name);

   		/*
   				Calculating the Instability and Stability
   		*/
   		var associations = tools.collectElements(classes[i], "_type", "UMLAssociation");
   		var generalization = tools.collectElements(classes[i], "_type", "UMLGeneralization");
   		var ops = tools.collectElements(classes[i], "_type", "UMLOperation");
   		var attributes = tools.collectElements(classes[i], "_type", "UMLAttribute");
   		var interfaceExtension = tools.collectElements(classes[i], "_type", "UMLInterfaceRealization");

   		//		Checking for the association
		if(associations.length != 0)
		{
			for(var k in associations)
			{
				var end2 = associations[k].end2.reference.$ref;
				for(var j in classes)
	   			{
		   			if(classes[i] != classes[j])
		   			{						
						if(classes[j]._id == end2 && instabilityArray.indexOf(classes[j].name) == -1)
							instabilityArray.push(classes[j].name);
					}
				}

				for(var j in interfaces)
				{
					if(end2 == interfaces[j]._id && instabilityArray.indexOf(interfaces[j].name) == -1)
						instabilityArray.push(interfaces[j].name);
				}
			}
		}

		//		Checking for the generalization
		if(generalization.length != 0)
		{
			for(var j in classes)
   			{
	   			if(classes[i] != classes[j])
	   			{
					for(var k in generalization)
					{
						var end2 = generalization[k].target.$ref;
						if(classes[j]._id == end2 && instabilityArray.indexOf(classes[j].name) == -1)
							instabilityArray.push(classes[j].name);
					}
				}
			}
		}

		//		Checking for the methods
		if(ops.length != 0)
		{
			for(var k in ops)
			{
				var parameters = tools.collectElements(ops[k], "_type", "UMLParameter");
				if(parameters.length != 0)
				{
					for(var l in parameters)
					{
						var param = parameters[l].type.$ref;
						for(var j in classes)
						{
							if(classes[i] != classes[j])
							{
								if(param == classes[j]._id && instabilityArray.indexOf(classes[j].name) == -1)
								  	instabilityArray.push(classes[j].name);
							}
						}

						for(var j in interfaces)
						{
							if(param == interfaces[j]._id && instabilityArray.indexOf(interfaces[j].name) == -1)
								instabilityArray.push(interfaces[j].name);
						}
					}
				}
			}
		}

		//		Checking for the attributes
		if(attributes.length != 0)
		{
			for(var k in attributes)
			{
				var attrib = attributes[k].type.$ref;
				for(var j in classes)
   				{
	   				if(classes[i] != classes[j])
	   				{
						if(attrib == classes[j]._id && instabilityArray.indexOf(classes[j].name) == -1)
							instabilityArray.push(classes[j].name);
					}
				}

				for(var j in interfaces)
				{
					if(attrib == interfaces[j]._id && instabilityArray.instabilityArray(interfaces[j].name) == -1)
						instabilityArray.push(interfaces[j].name);
				}
			}
		}
   		
   		//		Checking for the interface realization
   		if(interfaceExtension.length != 0)
   		{
   			for(var j in interfaceExtension)
   			{
   				var target = interfaceExtension[j].target.$ref;

   				for(var k in interfaces)
   				{
   					if(target == interfaces[k]._id && instabilityArray.indexOf(interfaces[k].name == -1))
   						instabilityArray.push(interfaces[k].name);
   				}
   			}
   		}

   		/*
   				Calculating the responsibility
   		*/
   		for(var j in classes)
   		{
   			if(classes[i] != classes[j])
   			{
   				var associations = tools.collectElements(classes[j], "_type", "UMLAssociation");
   				var generalization = tools.collectElements(classes[j], "_type", "UMLGeneralization");
   				var ops = tools.collectElements(classes[j], "_type", "UMLOperation");
   				var attributes = tools.collectElements(classes[j], "_type", "UMLAttribute"); 

   				//		Checking for the association
   				if(associations.length != 0)
   				{
   					for(var k in associations)
   					{
   						var end2 = associations[k].end2.reference.$ref;
   						if(end2 == classes[i]._id && responsibilityArray.indexOf(classes[j].name) == -1)
   							responsibilityArray.push(classes[j].name);
   					}
   				}

   				//		Checking the generalization
   				if(generalization.length != 0)
   				{
   					for(var k in generalization)
   					{
   						var end2 = generalization[k].target.$ref;
   						if(end2 == classes[i]._id && responsibilityArray.indexOf(classes[j].name) == -1)
   							responsibilityArray.push(classes[j].name);
   					}
   				}

   				//		Checking for the methods
   				if(ops.length != 0)
   				{
   					for(var k in ops)
   					{
   						var parameters = tools.collectElements(ops[k], "_type", "UMLParameter");
   						if(parameters.length != 0)
   						{
   							for(var l in parameters)
   							{
   								var param = parameters[l].type.$ref;
   								if(param == classes[i]._id && responsibilityArray.indexOf(classes[j].name) == -1)
   									responsibilityArray.push(classes[j].name);
   							}
   						}
   					}
   				}

   				//		Checking for the attributes
   				if(attributes.length != 0)
   				{
   					for(var k in attributes)
   					{
   						var attrib = attributes[k].type.$ref;
   						if(attrib == classes[i]._id && responsibilityArray.indexOf(classes[j].name) == -1)
   							responsibilityArray.push(classes[j].name);
   					}
   				}
   			}
   		}

   		if(interfaces.length != 0)
   		{
   			for(var j in interfaces)
   			{
   				var associations = tools.collectElements(interfaces[j], "_type", "UMLAssociation");
   				var ops = tools.collectElements(interfaces[j], "_type", "UMLOperation");
   				var attributes = tools.collectElements(interfaces[j], "_type", "UMLAttribute"); 
   				var interfaceRealization = tools.collectElements(interfaces[j], "_type", "UMLInterfaceRealization");

   				if(associations.length != 0)
   				{
   					for(var k in associations)
   					{
   						var end2 = associations[k].end2.reference.$ref;
   						if(end2 == classes[i]._id && responsibilityArray.indexOf(interfaces[j].name) == -1)
   							responsibilityArray.push(interfaces[j].name);
   					}
   				}

   				if(ops.length != 0)
   				{
   					for(var k in ops)
   					{
   						var parameters = tools.collectElements(ops[k], "_type", "UMLParameter");
   						if(parameters.length != 0)
   						{
   							for(var l in parameters)
   							{
   								var param = parameters[l].type.$ref;
   								if(param == classes[i]._id && responsibilityArray.indexOf(interfaces[j].name) == -1)
   									responsibilityArray.push(interfaces[j].name);
   							}
   						}
   					}
   				}

   				if(attributes.length != 0)
   				{
   					for(var k in attributes)
   					{
   						var attrib = attributes[k].type.$ref;
   						if(attrib == classes[i]._id && responsibilityArray.indexOf(interfaces[j].name) == -1)
   							responsibilityArray.push(interfaces[j].name);
   					}
   				}
   			}
   		}

   		if(bidirectionalClassArray.length != 0)
   		{
   			for(var m in bidirectionalClassArray)
   			{
   				var temp = bidirectionalClassArray[m].split("_");
   				if(classes[i]._id == temp[0])
   				{
   					for(var n in classes)
   					{
   						if(classes[n]._id == temp[1] && instabilityArray.indexOf(classes[n].name) == -1)
   							instabilityArray.push(classes[n].name);
   						if(classes[n]._id == temp[1] && responsibilityArray.indexOf(classes[n].name) == -1)
   							responsibilityArray.push(classes[n].name);

   					}
   				}

   				if(classes[i]._id == temp[1])
   				{
   					for(var n in classes)
   					{
   						if(classes[n]._id == temp[0] && instabilityArray.indexOf(classes[n].name) == -1)
   							instabilityArray.push(classes[n].name);
   						if(classes[n]._id == temp[0] && responsibilityArray.indexOf(classes[n].name) == -1)
   							responsibilityArray.push(classes[n].name)
   					}
   				}
   			}
   		}

   		var instability = instabilityArray.length / totalNoOfClasses;
   		var stability = 1 - instability;

   		var responsibility = responsibilityArray.length / totalNoOfClasses;


   		/*
   				Calculating the deviance
   		*/
   		var deviance = 0;
   		if(responsibility < stability)
   			deviance = stability - responsibility;
   		else
   			deviance = responsibility - stability;

   		/*console.log("responsibility = " + responsibility.toFixed(2));
   		//console.log("instability = " + instability);
   		console.log("stability = " + stability.toFixed(2));
   		console.log("deviance = " + deviance.toFixed(2) + "\n");	*/	 		

   		console.log(classes[i].name + "\t\t\t" + responsibility.toFixed(2) + "\t\t\t" + stability.toFixed(2) + "\t\t\t" + deviance.toFixed(2));
   }

   /*
   			Calculating design metrics for the interface
   	*/
   	for(var i in interfaces)
   	{
   		var instabilityArray = new Array(0);
   		var responsibilityArray = new Array(0);

   		//console.log("Current interface --> " + interfaces[i].name);

   		/*
   				Calculating the instability and stability
   		*/
   		var associations = tools.collectElements(interfaces[i], "_type", "UMLAssociation");
   		var generalization = tools.collectElements(interfaces[i], "_type", "UMLGeneralization");
   		var ops = tools.collectElements(interfaces[i], "_type", "UMLOperation");
   		var attributes = tools.collectElements(interfaces[i], "_type", "UMLAttribute");
   		var interfaceExtension = tools.collectElements(interfaces[i], "_type", "UMLInterfaceRealization");	

   		//		Checking for the associations
   		if(associations.length != 0)
   		{
   			for(var j in associations)
   			{
   				var end2 = associations[j].end2.reference.$ref;

   				for(var k in classes)
   				{
   					if(end2 == classes[k]._id && instabilityArray.indexOf(classes[k].name) == -1)
   						instabilityArray.push(classes[k].name);
   				}

   				for(var k in interfaces)
   				{
   					if(interfaces[i] != interfaces[k])
   					{
   						if(end2 == interfaces[k]._id && instabilityArray.indexOf(interfaces[k].name) == -1)
   							instabilityArray.push(interfaces[k].name);
   					}
   				}
   			}
   		}

   		//		Checking for the generalization
   		if(generalization.length != 0)
   		{
   			for(var j in generalization)
   			{
   				var target = generalization[j].target.$ref;
   				for(var k in interfaces)
   				{
   					if(interfaces[i] != interfaces[k])
   					{
   						if(target == interfaces[k]._id && instabilityArray.indexOf(interfaces[k].name) == -1)
   							instabilityArray.push(interfaces[k].name);
   					}
   				}
   			}
   		}

   		//		Checking for the methods
   		if(ops.length != 0)
   		{
   			for(var k in ops)
			{
				var parameters = tools.collectElements(ops[k], "_type", "UMLParameter");
				if(parameters.length != 0)
				{
					for(var l in parameters)
					{
						var param = parameters[l].type.$ref;
						for(var j in classes)
						{
							if(param == classes[j]._id && instabilityArray.indexOf(classes[j].name) == -1)
								 instabilityArray.push(classes[j].name);
						}

						for(var j in interfaces)
						{
							if(interfaces[i] != interfaces[j])
							{
								if(param == interfaces[j]._id && instabilityArray.indexOf(interfaces[j].name) == -1)
									instabilityArray.push(interfaces[j].name);
							}
						}
					}
				}
			}
   		}

   		//		Checking for the attributes
   		if(attributes.length != 0)
   		{
   			for(var k in attributes)
			{
				var attrib = attributes[k].type.$ref;
				for(var j in classes)
   				{
					if(attrib == classes[j]._id && instabilityArray.indexOf(classes[j].name) == -1)
						instabilityArray.push(classes[j].name);
				}

				for(var j in interfaces)
				{
					if(interfaces[i] != interfaces[j])
					{
						if(attrib == interfaces[j]._id && instabilityArray.indexOf(interfaces[j].name) == -1)
							instabilityArray.push(interfaces[j].name);
					}
				}
			}
   		}

   		if(interfaceExtension.length != 0)
   		{
   			for(var j in interfaceExtension)
   			{
   				var target = interfaceExtension[j].target.$ref;
   				for(var k in interfaces)
   				{
   					if(interfaces[i] != interfaces[k])
   					{
   						if(target == interfaces[k]._id && instabilityArray.indexOf(interfaces[k].name) == -1)
							instabilityArray.push(interfaces[k].name);
   					}
   				}
   			}
   		}

   		var instability = instabilityArray.length / totalNoOfClasses;
   		var stability = 1 - instability;

   		/*
   				Calculating the responsibility
   		*/
   		for(var j in classes)
   		{
   			var associations = tools.collectElements(classes[j], "_type", "UMLAssociation");
			var attributes = tools.collectElements(classes[j], "_type", "UMLAttribute"); 
			var interfaceRealization = tools.collectElements(classes[j], "_type", "UMLInterfaceRealization");

			//		Checking for the association
			if(associations.length != 0)
			{
				for(var k in associations)
				{
					var end2 = associations[k].end2.reference.$ref;
					if(end2 == interfaces[i]._id && responsibilityArray.indexOf(classes[j].name) == -1)
						responsibilityArray.push(classes[j].name);
				}
			}

			//		Checking for the attributes
			if(attributes.length != 0)
			{
				for(var k in attributes)
				{
					var attrib = attributes[k].type.$ref;
					if(attrib == interfaces[i]._id && responsibilityArray.indexOf(classes[j].name) == -1)
						responsibilityArray.push(classes[j].name);
				}
			}

			//		Checking for the interface realization
			if(interfaceRealization != 0)
			{
				for(var k in interfaceRealization)
				{
					var target = interfaceRealization[k].target.$ref;
					if(target == interfaces[i]._id && responsibilityArray.indexOf(classes[j].name) == -1)
						responsibilityArray.push(classes[j].name);
				}
			}
   		}

   		for(var j in interfaces)
   		{
   			if(interfaces[i] != interfaces[j])
   			{
   				var associations = tools.collectElements(interfaces[j], "_type", "UMLAssociation");
				var generalization = tools.collectElements(interfaces[j], "_type", "UMLGeneralization");
				var attributes = tools.collectElements(interfaces[j], "_type", "UMLAttribute"); 
				var interfaceRealization = tools.collectElements(interfaces[j], "_type", "UMLInterfaceRealization");

				//		Checking for the association
				if(associations.length != 0)
				{
					for(var k in associations)
					{
						var end2 = associations[k].end2.reference.$ref;
						if(end2 == interfaces[i]._id && responsibilityArray.indexOf(interfaces[j].name) == -1)
							responsibilityArray.push(interfaces[j].name);
					}
				}

				//		Checking for the generalization
				if(generalization.length != 0)
				{
					for(var k in generalization)
					{
						var target = generalization[k].target.$ref;
						if(target == interfaces[i]._id && responsibilityArray.indexOf(interfaces[j].name) == -1)
							responsibilityArray.push(interfaces[j].name);
					}
				}

				//		Checking for the attributes
				if(attributes.length != 0)
				{
					for(var k in attributes)
					{
						var attrib = attributes[k].type.$ref;
						if(attrib == interfaces[i]._id && responsibilityArray.indexOf(interfaces[j].name) == -1)
							responsibilityArray.push(interfaces[j].name);
					}
				}

				//		Checking for the interface realization
				if(interfaceRealization != 0)
				{
					for(var k in interfaceRealization)
					{
						var target = interfaceRealization[k].target.$ref;
						if(target == interfaces[i]._id && responsibilityArray.indexOf(interfaces[j].name) == -1)
							responsibilityArray.push(interfaces[j].name);
					}
				}
   			}
   		}

   		var responsibility = responsibilityArray.length / totalNoOfClasses;

   		/*
   				Calculating the deviance
   		*/
   		var deviance = 0;
   		if(responsibility < stability)
   			deviance = stability - responsibility;
   		else
   			deviance = responsibility - stability;

   		/*console.log("responsibility = " + responsibility.toFixed(2));
   		//console.log("instability = " + instability);
   		console.log("stability = " + stability.toFixed(2));
   		console.log("deviance = " + deviance.toFixed(2) + "\n");*/

   		console.log(interfaces[i].name + "\t\t\t" + responsibility.toFixed(2) + "\t\t\t" + stability.toFixed(2) + "\t\t\t" + deviance.toFixed(2));		
   	}
};

var main = function() {
   var readline = require('readline');
   var rl = readline.createInterface({
      input: process.stdin,
      output: process.stdout
   });

   rl.question("Enter file name: ", function(answer) {
      calculateMetrics(answer);
      rl.close();
    });
};


main();
counter = 0;

.qsardb.beforeEvaluate = function(){
	return (paste("Number of objects before:", length(ls(all = TRUE))))
}

.qsardb.afterEvaluate = function(){
	counter = (counter + 1)

	if(counter %% 10 == 0){
		gc()
	}

	return (paste("Number of objects after:", length(ls(all = TRUE))))
}
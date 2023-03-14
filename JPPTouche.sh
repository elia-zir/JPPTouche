while getopts i:o:d:r: flag
do
    # shellcheck disable=SC2220
    case "${flag}" in
        i) input=${OPTARG};;
        o) output=${OPTARG};;
	d) indexdir=${OPTARG};;
	r) runname=${OPTARG};;
    esac
done

java -cp ./*.jar it.unipd.dei.jpp.ToucheIR $input $output $indexdir $runname
/**
 * Created by miche on 26/10/2016.
 */
$(document).ready(function(){
    $("#stop").hide();
    $("#start").click(function(){
        $(this).toggle();
        $('#stop').toggle();
    });
    $("#stop").click(function(){
        $(this).toggle();
        $('#start').toggle();
    });
});
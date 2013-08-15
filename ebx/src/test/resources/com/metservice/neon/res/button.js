$(function(){
  $('#testButton').click(function(){
    var checkedValue = $('[name="rg"]:radio:checked').val();
    $('#result').html('The radio element with value <tt>' + checkedValue + '</tt> is checked.');
  });
});

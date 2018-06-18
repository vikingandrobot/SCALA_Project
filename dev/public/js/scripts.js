$(document).ready(function() {
   $('.search-type-selection').click(function(e) {
       e.preventDefault();

       if ($('#filterEvent').hasClass('open')) {
           $('.search-type-selection').html('Advanced search...');
           $('#filterEvent').removeClass('open');
           $('#date_field input').val('');
           $('#endDate_field input').val('');
       } else {
           $('#filterEvent').addClass('open');
           $('.search-type-selection').html('Normal search...');
       }
   })
});
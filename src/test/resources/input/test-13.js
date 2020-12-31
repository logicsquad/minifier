onSuccess: function(transport) {
            var js = transport.responseText.strip();
            if (!/^\[.*\]$/.test(js)) // TODO: improve sanity check
              throw 'Server returned an invalid collection representation.';
            this._collection = eval(js);
            this.checkForExternalText();
          }.bind(this),
          onFailure: this.onFailure
        });
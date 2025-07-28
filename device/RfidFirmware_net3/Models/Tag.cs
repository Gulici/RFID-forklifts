namespace RfidFirmware.Models
{
    public class Tag
    {
        private string _tag;

        public int AntennaNr { get; set; }

        public string Epc
        {
            get => _tag.ToUpper();
            set => _tag = value.ToUpper();
        }
    }
}
